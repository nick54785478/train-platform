package com.example.demo.iface.rest;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.iface.dto.res.StopDetailQueriedResource;
import com.example.demo.service.TrainStopQueryService;
import com.example.demo.util.BaseDataTransformer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/train/stops")
@Tag(name = "Train Stop API", description = "進行與火車車站領域相關動作")
public class TrainStopController {

	private TrainStopQueryService trainStopQueryService;

	/**
	 * 查詢該車次停靠站的詳細資訊(含各停靠站及其票價)
	 * 
	 * @param uuid     火車唯一代碼
	 * @param fromStop 起站
	 */
	@GetMapping("/details")
	@Operation(summary = "API - 查詢該車次停靠站的詳細資訊(含各停靠站及其票價)", description = "查詢該車次停靠站的詳細資訊(含各停靠站及其票價)。")
	public ResponseEntity<List<StopDetailQueriedResource>> getStopDetails(
			@Parameter(description = "UUID") @RequestParam String uuid,
			@Parameter(description = "起站") @RequestParam String fromStop) {
		return new ResponseEntity<>(BaseDataTransformer.transformData(
				trainStopQueryService.getStopDetails(uuid, fromStop), StopDetailQueriedResource.class), HttpStatus.OK);
	}
}
