package com.example.demo.domain.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.example.demo.application.shared.exception.SeatsSoldOutException;
import com.example.demo.base.application.port.BusinessIdempotenceExecutorPort;
import com.example.demo.base.domain.service.BaseDomainService;
import com.example.demo.base.shared.command.ExecuteIdempotenceCommand;
import com.example.demo.base.shared.enums.YesNo;
import com.example.demo.domain.seat.aggregate.TrainSeat;
import com.example.demo.domain.setting.aggregate.ConfigurableSetting;
import com.example.demo.domain.shared.dto.UnbookedSeatGottenView;
import com.example.demo.infra.repository.SettingRepository;
import com.example.demo.infra.repository.TrainSeatRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class SeatService extends BaseDomainService {

	private SettingRepository settingRepository;
	private TrainSeatRepository trainSeatRepository;
	private BusinessIdempotenceExecutorPort idempotentExecutor;

	/**
	 * 取得座位代號及車廂編號
	 * 
	 * @param trainUuid 火車唯一值
	 * @param takeDate  搭乘日期
	 * @return TrainSeatGottenData 可搭乘車位資訊
	 */
	public UnbookedSeatGottenView getUnbookedSeat(String trainUuid, LocalDate takeDate) {
		UnbookedSeatGottenView trainSeatGottenData = new UnbookedSeatGottenView();
		trainSeatGottenData.setTrainUuid(trainUuid);

		Map<Long, List<String>> availableSeatsMap = new HashMap<>();

		// 1. 取得全座位清單與全車廂清單設定 (這部分邏輯維持不變)
		List<ConfigurableSetting> seatNoSetting = settingRepository.findByDataTypeAndActiveFlag("SEAT_NO_LIST",
				YesNo.Y);
		List<String> seatNoList = seatNoSetting.stream().flatMap(s -> Arrays.stream(s.getValue().split(",")))
				.map(String::trim).collect(Collectors.toList());

		List<ConfigurableSetting> carNoSettings = settingRepository.findByDataTypeAndActiveFlag("CAR_NO_LIST", YesNo.Y);
		List<Long> carNoList = carNoSettings.stream().flatMap(setting -> Arrays.stream(setting.getValue().split(",")))
				.map(String::trim).map(Long::valueOf).collect(Collectors.toList());

		// 2. 查詢目前已被佔用的座位 (已預訂 + 已取消但尚未釋放的)
		List<TrainSeat> trainSeats = trainSeatRepository.findByTrainUuidAndTakeDateAndActiveFlag(trainUuid, takeDate,
				YesNo.Y);
		List<TrainSeat> cancelledSeats = trainSeatRepository.findByTakeDateAndTrainUuidAndBookedAndActiveFlag(takeDate,
				trainUuid, YesNo.N, YesNo.N);
		trainSeats.addAll(cancelledSeats);

		// 3. 計算各車廂剩餘可用座位
		if (trainSeats.isEmpty()) {
			availableSeatsMap = carNoList.stream()
					.collect(Collectors.toMap(carNo -> carNo, carNo -> new ArrayList<>(seatNoList)));
		} else {
			Map<Long, Set<String>> bookedSeatsMap = trainSeats.stream().collect(Collectors
					.groupingBy(TrainSeat::getCarNo, Collectors.mapping(TrainSeat::getSeatNo, Collectors.toSet())));

			availableSeatsMap = carNoList.stream().collect(Collectors.toMap(carNo -> carNo, carNo -> {
				Set<String> bookedSeats = bookedSeatsMap.getOrDefault(carNo, Collections.emptySet());
				return seatNoList.stream().filter(seat -> !bookedSeats.contains(seat)).collect(Collectors.toList());
			}));
		}

		// 4. 遍歷車廂嘗試搶位
        for (Map.Entry<Long, List<String>> entry : availableSeatsMap.entrySet()) {
            Long carNo = entry.getKey();
            List<String> unbooked = entry.getValue();

            while (Objects.isNull(trainSeatGottenData.getCarNo()) && StringUtils.isBlank(trainSeatGottenData.getSeatNo())) {
                this.getSeatNoAndCarNo(trainSeatGottenData, unbooked, carNo, takeDate);
                if (unbooked.isEmpty()) break;
            }

            if (Objects.nonNull(trainSeatGottenData.getCarNo())) break;
        }

        // 【新增：售罄檢查邏輯】
        // 如果跑完了所有車廂 (for) 且所有剩餘座位 (while)，結果依然是空的
        if (Objects.isNull(trainSeatGottenData.getCarNo())) {
            log.warn("車次預訂失敗：{} 於 {} 的座位已售罄或正在激烈的併發搶購中", trainUuid, takeDate);
            
            // 拋出業務異常，這會導致當前的 Transaction 回滾
            throw new SeatsSoldOutException(trainUuid, takeDate);
        }

		return trainSeatGottenData;
	}

	/**
	 * 遞迴取得 SeatNo(座位編號) 及 CarNo(車廂編號)，避免在分布式環境中衝突取號
	 * 
	 * * @param trainSeatGottenData 承載結果的 View
	 * 
	 * @param unbooked 當前車廂未被預訂的座位清單
	 * @param carNo    目前正在遍歷的車廂編號
	 * @param takeDate 乘車日期 (由上層傳入，用於產生唯一 Key)
	 */
	private void getSeatNoAndCarNo(UnbookedSeatGottenView trainSeatGottenData, List<String> unbooked, Long carNo,
			LocalDate takeDate) {
		// 只有在還沒取得座位時才執行
		if (Objects.isNull(trainSeatGottenData.getCarNo()) && StringUtils.isBlank(trainSeatGottenData.getSeatNo())) {

			if (unbooked.isEmpty())
				return;

			// 嘗試從清單中取號
			unbooked.stream().findAny().ifPresent(seatNo -> {

				// 【修正】使用傳入的參數產生 Key，因為此時 trainSeatGottenData 裡面還是空的
				String businessKey = this.generateBusinessKey(trainSeatGottenData.getTrainUuid(), takeDate, carNo,
						seatNo);

				// 構建冪等指令
				ExecuteIdempotenceCommand command = ExecuteIdempotenceCommand.builder().eventLogUuid(businessKey)
						.targetId(seatNo).build();

				// 執行冪等校驗（如 Redis SETNX 或 DB 唯一索引）
				if (idempotentExecutor.execute(command)) {
					// 成功搶到位置，填入結果
					trainSeatGottenData.setCarNo(carNo);
					trainSeatGottenData.setSeatNo(seatNo);
					// 既然搶到了，從本地暫存清單移除
					unbooked.remove(seatNo);
				}
			});
		}
	}

	/**
	 * 產生具備業務唯一性的冪等 Key 格式範例: SEAT_CLAIM:trainUuid:takeDate:carNo:seatNo
	 * 
	 * @param trainUuid 列車唯一識別碼
	 * @param takeDate  乘車日期
	 * @param carNo     車廂編號
	 * @param seatNo    座位編號
	 * @return 唯一識別字串
	 */
	private String generateBusinessKey(String trainUuid, LocalDate takeDate, Long carNo, String seatNo) {
		return String.format("SEAT_CLAIM:%s:%s:%d:%s", trainUuid, takeDate.toString(), carNo, seatNo);
	}
}
