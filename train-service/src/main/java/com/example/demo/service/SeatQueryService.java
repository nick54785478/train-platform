package com.example.demo.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.base.application.service.BaseApplicationService;
import com.example.demo.domain.service.SeatService;
import com.example.demo.domain.share.SeatQueriedData;
import com.example.demo.domain.share.UnbookedSeatGottenData;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class SeatQueryService extends BaseApplicationService {

	private SeatService seatService;

	/**
	 * 查詢該乘車時段已被預訂的車位
	 * 
	 * @param trainUuid
	 * @param takeDate
	 * @return 車位資料
	 */
	public List<SeatQueriedData> queryBookedSeats(String trainUuid, LocalDate takeDate) {
		return seatService.queryBookedSeats(trainUuid, takeDate);
	}

	/**
	 * 取得座位代號及車廂編號
	 * 
	 * @param trainUuid
	 * @param takeDate
	 * @return TrainSeatGottenData
	 */
	public UnbookedSeatGottenData getUnbookedSeat(String trainUuid, LocalDate takeDate) {
		return seatService.getUnbookedSeat(trainUuid, takeDate);
	}
}
