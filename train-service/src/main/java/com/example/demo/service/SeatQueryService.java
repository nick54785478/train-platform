package com.example.demo.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.application.shared.dto.SeatQueriedData;
import com.example.demo.base.application.service.BaseApplicationService;
import com.example.demo.base.shared.enums.YesNo;
import com.example.demo.domain.seat.aggregate.TrainSeat;
import com.example.demo.domain.service.SeatService;
import com.example.demo.domain.share.dto.UnbookedSeatGottenView;
import com.example.demo.infra.repository.TrainSeatRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class SeatQueryService extends BaseApplicationService {

	private SeatService seatService;
	private TrainSeatRepository trainSeatRepository;

	/**
	 * 查詢該乘車時段已被預訂的車位
	 * 
	 * @param trainUuid
	 * @param takeDate
	 * @return 車位資料
	 */
	public List<SeatQueriedData> queryBookedSeats(String trainUuid, LocalDate takeDate) {
		List<TrainSeat> trainSeats = trainSeatRepository.findByTakeDateAndTrainUuidAndBookedAndActiveFlag(takeDate,
				trainUuid, YesNo.Y, YesNo.Y);
		return this.transformData(trainSeats, SeatQueriedData.class);
	}

	/**
	 * 取得座位代號及車廂編號
	 * 
	 * @param trainUuid
	 * @param takeDate
	 * @return TrainSeatGottenData
	 */
	public UnbookedSeatGottenView getUnbookedSeat(String trainUuid, LocalDate takeDate) {
		return seatService.getUnbookedSeat(trainUuid, takeDate);
	}
}
