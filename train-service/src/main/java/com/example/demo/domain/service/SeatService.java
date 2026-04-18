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
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.example.demo.base.application.port.EventIdempotenceHandlerPort;
import com.example.demo.base.domain.service.BaseDomainService;
import com.example.demo.base.shared.command.BaseIdempotentCommand;
import com.example.demo.base.shared.enums.YesNo;
import com.example.demo.domain.seat.aggregate.TrainSeat;
import com.example.demo.domain.setting.aggregate.ConfigurableSetting;
import com.example.demo.domain.share.dto.UnbookedSeatGottenView;
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
	private EventIdempotenceHandlerPort eventIdempotentLogService;

	/**
	 * 取得座位代號及車廂編號
	 * 
	 * @param trainUuid
	 * @param takeDate
	 * @return TrainSeatGottenData
	 */
	public UnbookedSeatGottenView getUnbookedSeat(String trainUuid, LocalDate takeDate) {
		UnbookedSeatGottenView trainSeatGottenData = new UnbookedSeatGottenView();
		trainSeatGottenData.setTrainUuid(trainUuid);

		Map<Long, List<String>> availableSeatsMap = new HashMap<>();

		// 取得全座位清單設定
		List<ConfigurableSetting> seatNoSetting = settingRepository.findByDataTypeAndActiveFlag("SEAT_NO_LIST",
				YesNo.Y);
		// 解析座位清單設定 (假設座位清單是逗號分隔的字串，如 "A1,A2,A3,...")
		List<String> seatNoList = seatNoSetting.stream().flatMap(s -> Arrays.stream(s.getValue().split(",")))
				.collect(Collectors.toList());

		// 取得全車廂編號清單設定
		List<ConfigurableSetting> carNoSettings = settingRepository.findByDataTypeAndActiveFlag("CAR_NO_LIST", YesNo.Y);
		// 車廂編號清單
		List<Long> carNoList = carNoSettings.stream().flatMap(setting -> Arrays.stream(setting.getValue().split(","))) // 將設定值拆分為單一編號
				.map(String::trim) // 移除可能存在的空格
				.map(Long::valueOf) // 將字串轉換為 Long
				.collect(Collectors.toList());

		// 查詢內容目前被預訂清單
		List<TrainSeat> trainSeats = trainSeatRepository.findByTrainUuidAndTakeDateAndActiveFlag(trainUuid, takeDate,
				YesNo.Y);

		// 查詢目前被取消訂位的預訂清單
		List<TrainSeat> cancelledSeats = trainSeatRepository.findByTakeDateAndTrainUuidAndBookedAndActiveFlag(takeDate,
				trainUuid, YesNo.N, YesNo.N);

		// 合併兩者(目前被預訂及被取消的訂位資料)
		trainSeats.addAll(cancelledSeats);

		// 如果 trainSeats 為空，直接生成所有座位未被預訂的 Map
		if (trainSeats.isEmpty()) {
			availableSeatsMap = carNoList.stream().collect(Collectors.toMap(carNo -> carNo, // 車廂編號為 key
					carNo -> new ArrayList<>(seatNoList) // 所有座位都未被預訂
			));
		} else {
			// 將已預訂座位轉為 Map<Long, Set<String>> (車廂編號 -> 已預訂座位)
			Map<Long, Set<String>> bookedSeatsMap = trainSeats.stream()
					.collect(Collectors.groupingBy(TrainSeat::getCarNo, // 依車廂編號分組
							Collectors.mapping(TrainSeat::getSeatNo, Collectors.toSet()) // 收集每個車廂的已預訂座位
					));

			// 建立未被預訂的清單 Map<Integer, List<String>>
			availableSeatsMap = carNoList.stream().collect(Collectors.toMap(carNo -> carNo, // 車廂編號為 key
					carNo -> {
						// 該車廂的已預訂座位
						Set<String> bookedSeats = bookedSeatsMap.getOrDefault(carNo, Collections.emptySet());
						// 過濾出未被預訂的座位
						return seatNoList.stream().filter(seat -> !bookedSeats.contains(seat)) // 排除已被預訂的座位
								.collect(Collectors.toList());
					}));
		}

		for (Map.Entry<Long, List<String>> entry : availableSeatsMap.entrySet()) {
			// 車廂編號
			Long key = entry.getKey();
			// 該車廂尚未被預訂座位清單
			List<String> unbooked = entry.getValue();

			// 透過 while 迴圈嘗試取 SeatNo 及 CarNo 直到取得
			while (Objects.isNull(trainSeatGottenData.getCarNo())
					&& StringUtils.isBlank(trainSeatGottenData.getSeatNo())) {
				// 取得 SeatNo
				this.getSeatNoAndCarNo(trainSeatGottenData, unbooked, key);
				// 若 unbooked 清單為空，跳出 while 迴圈，代表以該車廂已無未預定的座位
				if (unbooked.isEmpty()) {
					break;
				}
			}

			// CarNo 及 SeatNo 已取得，跳出迴圈
			if (!Objects.isNull(trainSeatGottenData.getCarNo())
					&& StringUtils.isNotBlank(trainSeatGottenData.getSeatNo())) {
				break;
			}
		}
		return trainSeatGottenData;
	}

	/**
	 * 遞迴取得 SeatNo(座位編號) 及 CarNo(車廂編號)，避免在分布式環境中衝突取號
	 * 
	 * @param trainNoGottenData 回傳結果
	 * @param unbooked          未被預訂的位置清單
	 * @param key               車廂編號
	 */
	private void getSeatNoAndCarNo(UnbookedSeatGottenView trainSeatGottenData, List<String> unbooked, Long key) {

		// 若 CarNo 與 SeatNo 未取得，進入執行
		if (Objects.isNull(trainSeatGottenData.getCarNo()) && StringUtils.isBlank(trainSeatGottenData.getSeatNo())) {

			// 如果 Unbooked 清單為空跳出，代表以該車廂已無未預定的座位
			if (unbooked.isEmpty()) {
				return;
			}

			unbooked.stream().findAny().ifPresent(seatNo -> {
				// 使用冪等機制
				BaseIdempotentCommand command = BaseIdempotentCommand.builder()
						.eventLogUuid(UUID.randomUUID().toString()).targetId(seatNo).build();
				if (eventIdempotentLogService.handleIdempotency(command)) {
					trainSeatGottenData.setCarNo(key); // 根據目前被遍歷的車廂編號設置
					trainSeatGottenData.setSeatNo(seatNo); // 設置隨機一筆 SeatNo
					// 設置完移除該 SeatNo
					unbooked.remove(seatNo);
				}
			});
		}
	}
}
