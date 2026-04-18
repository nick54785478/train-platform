package com.example.demo.iface.rest;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.application.shared.dto.SeatQueriedData;
import com.example.demo.domain.share.dto.UnbookedSeatGottenView;
import com.example.demo.iface.dto.res.SeatQueriedResource;
import com.example.demo.iface.dto.res.UnbookedSeatGottenResource;
import com.example.demo.service.SeatQueryService;
import com.example.demo.util.BaseDataTransformer;
import com.example.demo.util.DateTransformUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/seats")
@Tag(name = "Seat API", description = "進行與座位領域相關動作")
public class SeatController {

	private SeatQueryService seatQueryService;

	/**
	 * 查詢該乘車時段的該車次已被預訂的座位
	 * 
	 * @param trainUuid
	 * @param takeDate
	 * @return 車位資料
	 */
	@GetMapping("")
	@Operation(summary = "API - 查詢該乘車時段的該車次已被預訂的座位", description = "查詢該乘車時段的該車次已被預訂的座位。")
	public ResponseEntity<List<SeatQueriedResource>> queryBookedSeats(
			@Parameter(description = "火車代號 uuid") @RequestParam String trainUuid,
			@Parameter(description = "乘車日期 (yyyy-mm-dd)") @RequestParam String takeDate) {
		List<SeatQueriedData> seats = seatQueryService.queryBookedSeats(trainUuid,
				DateTransformUtil.transformStringToLocalDate(takeDate));
		return new ResponseEntity<>(BaseDataTransformer.transformData(seats, SeatQueriedResource.class), HttpStatus.OK);
	}

	/**
	 * 取得座位資料
	 * 
	 * @param trainUuid
	 * @param takeDate
	 * @return 車位資料
	 */
	@GetMapping("/unbooked")
	@Operation(summary = "API - 取得未被預訂的座位資料", description = "取得未被預訂的座位資料。")
	public ResponseEntity<UnbookedSeatGottenResource> getUnbookedTrainSeat(
			@Parameter(description = "火車代號 uuid") @RequestParam String trainUuid,
			@Parameter(description = "乘車日期 (yyyy-mm-dd)") @RequestParam String takeDate) {
		UnbookedSeatGottenView unbookedSeat = seatQueryService.getUnbookedSeat(trainUuid,
				DateTransformUtil.transformStringToLocalDate(takeDate));
		return new ResponseEntity<>(BaseDataTransformer.transformData(unbookedSeat, UnbookedSeatGottenResource.class),
				HttpStatus.OK);
	}

}
