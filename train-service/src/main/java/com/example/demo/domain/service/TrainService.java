package com.example.demo.domain.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.example.demo.base.domain.service.BaseDomainService;
import com.example.demo.base.shared.enums.YesNo;
import com.example.demo.base.shared.exception.exception.ValidationException;
import com.example.demo.domain.setting.aggregate.ConfigurableSetting;
import com.example.demo.domain.share.dto.StopSummaryQueriedView;
import com.example.demo.domain.share.dto.TrainDetailQueriedView;
import com.example.demo.domain.share.dto.TrainQueriedView;
import com.example.demo.domain.share.dto.TrainSummaryQueriedView;
import com.example.demo.domain.ticket.aggregate.Ticket;
import com.example.demo.domain.train.aggregate.Train;
import com.example.demo.domain.train.aggregate.entity.TrainStop;
import com.example.demo.domain.train.aggregate.vo.TrainKind;
import com.example.demo.domain.train.command.CreateTrainCommand;
import com.example.demo.domain.train.command.QueryTrainCommand;
import com.example.demo.domain.train.command.UpdateTrainCommand;
import com.example.demo.domain.train.query.SummaryTrainQuery;
import com.example.demo.infra.repository.SettingRepository;
import com.example.demo.infra.repository.TicketRepository;
import com.example.demo.infra.repository.TrainRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Domain Service
 */
@Slf4j
@Service
@AllArgsConstructor
public class TrainService extends BaseDomainService {

	private TrainRepository trainRepository;
	private TicketRepository ticketRepository;
	private SettingRepository settingRepository;

	/**
	 * 新增火車資料
	 * 
	 * @param command
	 */
	public void create(CreateTrainCommand command) {
		this.checkBeforeCreate(command);
		Train train = new Train();
		train.create(command);
		trainRepository.save(train);
	}

	/**
	 * 更新火車資料
	 * 
	 * @param command
	 */
	public void update(UpdateTrainCommand command) {
		Train train = trainRepository.findByNumber(command.getTrainNo());
		train.update(command, train.getUuid());
		trainRepository.save(train);
	}

//	/**
//	 * 透過號次查詢該火車資訊
//	 * 
//	 * @param trainNo
//	 * @return 火車資訊
//	 */
//	@Transactional // 確保在整個方法執行期間 Session 是打開的，保持懶加載(否則會報錯)
//	public TrainQueriedData query(Integer trainNo) {
//		Train train = trainRepository.findByNumber(trainNo);
//		if (Objects.isNull(train)) {
//			throw new ValidationException("VALIDATE_FAILED", "查無此車次 " + trainNo);
//		}
//		TrainQueriedData queriedData = this.transformAggregate(train, TrainQueriedData.class);
//		queriedData.getStops().sort(Comparator.comparingInt(StopQueriedData::getSeq));
//		return queriedData;
//
//	}

	/**
	 * 透過條件過濾並查詢火車資訊(供訂票查詢用)
	 * 
	 * @param command
	 * @return List<TrainDetailQueriedData>
	 */
	public List<TrainDetailQueriedView> queryTrainInfo(QueryTrainCommand command) {
		List<TrainDetailQueriedView> resList = new ArrayList<>();
		List<Train> trainList = trainRepository.findByCondition(command.getTrainNo(),
				StringUtils.isNotBlank(command.getTrainKind()) ? TrainKind.fromLabel(command.getTrainKind()).toString()
						: null,
				command.getTime(), command.getFromStop(), command.getToStop());

		// 各車票種類的價格折扣
		Map<String, BigDecimal> rateMap = settingRepository.findByDataTypeAndActiveFlag("TICKET_PRICE_RATE", YesNo.Y)
				.stream()
				.collect(Collectors.toMap(ConfigurableSetting::getName, setting -> new BigDecimal(setting.getValue())));

		// 建立 Train uuid 清單
		List<String> trainNoList = trainList.stream().map(Train::getUuid).collect(Collectors.toList());

		// 查詢車票資料，並整理為 Map<車次uuid-起站-迄站, Ticket>
		List<Ticket> ticketList = ticketRepository.findByTrainUuidIn(trainNoList);
		Map<String, Ticket> ticketMap = ticketList.stream()
				.collect(Collectors.toMap(
						ticket -> ticket.getTrainUuid() + "-" + ticket.getFromStop() + "-" + ticket.getToStop(),
						Function.identity()));

		// 透過 flatMap 與 Collectors.toMap 取得 Map<trainUuid+stopName, TrainStop>
		Map<String, TrainStop> stopMap = trainList.stream().flatMap(
				train -> train.getStops().stream().map(stop -> Map.entry(train.getUuid() + "-" + stop.getName(), stop)))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		// 遍歷火車資料進行資料更新
		trainList.stream().forEach(e -> {
			TrainDetailQueriedView trainData = new TrainDetailQueriedView();
			trainData.setUuid(e.getUuid());
			trainData.setTrainNo(e.getNumber());
			trainData.setKind(e.getKind().getLabel());
			trainData.setTakeDate(command.getTakeDate());

			// 從 stopMap 中取出對應的起訖站資料
			// 取得起站資料
			String fromStopkey = e.getUuid() + "-" + command.getFromStop();
			if (!Objects.isNull(stopMap.get(fromStopkey))) {
				var fromStop = stopMap.get(fromStopkey);
				trainData.setFromStop(fromStop.getName());
				trainData.setFromStopTime(fromStop.getTime());
			}
			// 取得迄站資料
			String toStopkey = e.getUuid() + "-" + command.getToStop();
			if (!Objects.isNull(stopMap.get(toStopkey))) {
				var toStop = stopMap.get(toStopkey);
				trainData.setToStop(toStop.getName());
				trainData.setToStopTime(toStop.getTime());
			}
			// 從 ticketMap 中取出對應的 Ticket 資料
			String ticketkey = e.getUuid() + "-" + command.getFromStop() + "-" + command.getToStop();
			if (!Objects.isNull(ticketMap.get(ticketkey))) {
				var ticket = ticketMap.get(ticketkey);
				trainData.setTicketUuid(ticket.getTicketNo());

				// 根據選擇的票別去打折
				if (!Objects.isNull(rateMap.get(command.getTicketType()))) {
					var rate = rateMap.get(command.getTicketType());
					trainData.setPrice(ticket.getPrice().multiply(rate).setScale(0, RoundingMode.HALF_UP));
				}
			}
			resList.add(trainData);
		});
		return resList;
	}

	/**
	 * 透過條件過濾並查詢火車資訊
	 * 
	 * @param command
	 * @return 火車資訊
	 */
	@Transactional // 確保在整個方法執行期間 Session 是打開的，保持懶加載(否則會報錯)
	public List<TrainSummaryQueriedView> summary(SummaryTrainQuery query) {
		List<TrainSummaryQueriedView> resList = new ArrayList<>();
		List<Train> trainList = trainRepository.findByCondition(query.getTrainNo(),
				StringUtils.isNotBlank(query.getTrainKind()) ? TrainKind.fromLabel(query.getTrainKind()).toString()
						: null,
				StringUtils.isNotBlank(query.getTime()) ? query.getTime() : "00:00:00", query.getFromStop(),
				query.getToStop());
		trainList.stream().forEach(e -> {
			TrainSummaryQueriedView trainData = new TrainSummaryQueriedView();
			trainData.setUuid(e.getUuid());
			trainData.setTrainNo(e.getNumber());
			trainData.setKind(e.getKind().getLabel());

			// 取得起點站與終點站
			TrainStop[] station = getFirstAndTerminatedStation(e.getStops());
			this.setStopData(station, trainData);

			List<StopSummaryQueriedView> stopResource = this.transformAggregate(e.getStops(),
					StopSummaryQueriedView.class);

			// 依 SEQ 升序排序
			stopResource.sort(Comparator.comparingInt(StopSummaryQueriedView::getSeq));
			trainData.setStops(stopResource);
			resList.add(trainData);
		});
		return resList;
	}

	/**
	 * 設置 Stop 資料
	 * 
	 * @param stationData 起始站與終點站資料
	 * @param trainData   火車查詢資料
	 */
	private void setStopData(TrainStop[] stationData, TrainSummaryQueriedView trainData) {
		TrainStop firstStop = stationData[0]; // 起點站
		TrainStop terminatedStop = stationData[1]; // 終點站
		trainData.setFromStop(firstStop.getName());
		trainData.setToStop(terminatedStop.getName());
		trainData.setFromStopTime(stationData[0].getTime()); // 起站時間
		trainData.setToStopTime(stationData[1].getTime());// 迄站時間

	}

	/**
	 * 透過停靠站查詢該火車資訊
	 * 
	 * @param fromStop 起站
	 * @param toStop   迄站
	 * @return 火車資訊
	 */
	@Transactional // 確保在整個方法執行期間 Session 是打開的，保持懶加載(否則會報錯)
	public List<TrainQueriedView> getTrainListBetweenStopSection(String fromStop, String toStop) {
		List<Train> trainList = trainRepository.findAll();

		return trainList.stream().filter(e -> {
			List<String> stopList = e.getStops().stream().sorted(Comparator.comparingInt(TrainStop::getSeq)) // 根據 SEQ
					.map(TrainStop::getName).collect(Collectors.toList());
			// 起站 index
			int fromIndex = stopList.indexOf(fromStop);
			// 迄站 index
			int toIndex = stopList.indexOf(toStop);

			// 確保起站存在，迄站存在，且 起站不能在迄站後面
			return fromIndex >= 0 && toIndex >= 0 && fromIndex < toIndex;
		}).map(e -> this.transformAggregate(e, TrainQueriedView.class)).collect(Collectors.toList());
	}

	/**
	 * 組合式檢核 i. 檢核車次是否已存在 ii. 檢核車種是否合法 iii. 檢核停靠站名是否重覆（相同站名不可重覆出現）
	 * 
	 * @param command
	 * @param true/false
	 */
	public Boolean checkBeforeCreate(CreateTrainCommand command) {

		// 檢核車次是否已存在 (車次不存在才能新增)
		Train train = trainRepository.findByNumber(command.getTrainNo());
		if (!Objects.isNull(train)) {
			log.error("火車車次:{} 已存在，新增失敗", command.getTrainNo());
			throw new ValidationException("VALIDATE_FAILED", "火車車次已存在，新增失敗");
		}

		// 檢核車種是否合法
		if (Boolean.FALSE.equals(TrainKind.checkTrainKind(command.getTrainKind()))) {
			log.error("火車車種:{} 不合法，新增失敗", command.getTrainKind());
			throw new ValidationException("VALIDATE_FAILED", "車種不合法，新增失敗");
		}

		// 檢核停靠站名是否重覆（相同站名不可重覆出現）
		long count = command.getStops().stream().distinct().count();
		if (command.getStops().size() != count) {
			log.error("火車停靠站重複，新增失敗，車次列表:{}", command.getStops());
			throw new ValidationException("VALIDATE_FAILED", "火車停靠站重複，新增失敗");
		}
		return true;
	}

	/**
	 * 找出起始站與終點站
	 * 
	 * @param trainStops 車站列表
	 * @return TrainStop[]
	 */
	private TrainStop[] getFirstAndTerminatedStation(List<TrainStop> trainStops) {
		return trainStops.stream().collect(() -> new TrainStop[] { null, null }, (res, stop) -> {
			if (res[0] == null || stop.getSeq() < res[0].getSeq()) {
				res[0] = stop; // 最小

			}
			if (res[1] == null || stop.getSeq() > res[1].getSeq()) {
				res[1] = stop; // 最大
			}
		}, (res1, res2) -> {
			if (res1[0] == null || (res2[0] != null && res2[0].getSeq() < res1[0].getSeq())) {
				res1[0] = res2[0];
			}
			if (res1[1] == null || (res2[1] != null && res2[1].getSeq() > res1[1].getSeq())) {
				res1[1] = res2[1];
			}
		});
	}

}
