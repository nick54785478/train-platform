package com.example.demo.domain.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.demo.base.shared.exception.exception.ValidationException;
import com.example.demo.domain.share.StopDetailQueriedData;
import com.example.demo.domain.ticket.aggregate.Ticket;
import com.example.demo.domain.train.aggregate.Train;
import com.example.demo.domain.train.aggregate.entity.TrainStop;
import com.example.demo.infra.repository.TicketRepository;
import com.example.demo.infra.repository.TrainRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class TrainStopService {

	private TrainRepository trainRepository;
	private TicketRepository ticketRepository;

	/**
	 * 查詢該車次停靠站的詳細資訊(含各停靠站及其票價)
	 * 
	 * @param uuid     火車唯一代碼
	 * @param fromStop 起站
	 */
	@Transactional
	public List<StopDetailQueriedData> getStopDetails(String uuid, String fromStop) {
		Optional<Train> opt = trainRepository.findById(uuid);
		if (opt.isEmpty()) {
			throw new ValidationException("VALIDATE_FAILED", "查無該車次資料");
		} else {
			Train train = opt.get();
			// Map<車站, 該車站資訊>
			Map<String, TrainStop> stopMap = train.getStops().stream()
					.collect(Collectors.toMap(TrainStop::getName, Function.identity()));

			// 查詢起站通往各迄站車票資訊
			List<Ticket> tickets = ticketRepository.findByTrainUuidAndFromStop(train.getUuid(), fromStop);
			return tickets.stream().map(ticket -> {
				StopDetailQueriedData stopDetailQueriedData = new StopDetailQueriedData();
				stopDetailQueriedData.setFromStop(fromStop);
				stopDetailQueriedData.setToStop(ticket.getToStop());

				// 取得起站資訊
				if (!Objects.isNull(stopMap.get(fromStop))) {
					TrainStop fromStopInfo = stopMap.get(ticket.getToStop());
					stopDetailQueriedData.setFromStop(fromStop);
					stopDetailQueriedData.setArriveStartStopTime(fromStopInfo.getTime().toString());
				}
				// 取得迄站資訊
				if (!Objects.isNull(stopMap.get(ticket.getToStop()))) {
					TrainStop toStopInfo = stopMap.get(ticket.getToStop());
					stopDetailQueriedData.setSeq(toStopInfo.getSeq());
					stopDetailQueriedData.setArriveStartStopTime(toStopInfo.getTime().toString()); // 抵達起站時間
					stopDetailQueriedData.setArriveEndStopTime(toStopInfo.getTime().toString()); // 抵達迄站時間
					stopDetailQueriedData.setPrice(ticket.getPrice()); // 票價
				}
				return stopDetailQueriedData;
			}).sorted(Comparator.comparingInt(StopDetailQueriedData::getSeq)).collect(Collectors.toList());
		}
	}

}
