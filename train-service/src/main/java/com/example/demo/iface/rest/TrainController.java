package com.example.demo.iface.rest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.application.service.TrainCommandService;
import com.example.demo.application.service.TrainQueryService;
import com.example.demo.base.shared.exception.exception.ValidationException;
import com.example.demo.domain.train.command.CreateTrainCommand;
import com.example.demo.domain.train.command.UpdateTrainCommand;
import com.example.demo.domain.train.query.GetTrainQuery;
import com.example.demo.domain.train.query.SummaryTrainQuery;
import com.example.demo.iface.dto.req.CreateTrainResource;
import com.example.demo.iface.dto.req.UpdateTrainResource;
import com.example.demo.iface.dto.res.TrainCreatedResource;
import com.example.demo.iface.dto.res.TrainDetailQueriedResource;
import com.example.demo.iface.dto.res.TrainQueriedResource;
import com.example.demo.iface.dto.res.TrainSummaryQueriedResource;
import com.example.demo.iface.dto.res.TrainUpdatedResource;
import com.example.demo.iface.dto.res.TrainUploadedResource;
import com.example.demo.util.BaseDataTransformer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Valid
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/train")
@Tag(name = "Train API", description = "進行與火車領域相關動作")
public class TrainController {

	private TrainQueryService trainQueryService;
	private TrainCommandService trainCommandService;

	/**
	 * 新增火車車次
	 * 
	 * @param resource
	 * @return 成功訊息
	 */
	@PostMapping("")
	@Operation(summary = "API - 新增火車車次", description = "新增火車車次。")
	public ResponseEntity<TrainCreatedResource> create(
			@Parameter(description = "火車車次資訊") @RequestBody CreateTrainResource resource) {
		// DTO 轉換
		CreateTrainCommand command = BaseDataTransformer.transformData(resource, CreateTrainCommand.class);
		trainCommandService.createTrain(command);
		return new ResponseEntity<>(new TrainCreatedResource("201", "新增車次成功"), HttpStatus.OK);
	}

	/**
	 * 更新火車車次
	 * 
	 * @param resource
	 * @return 成功訊息
	 */
	@PutMapping("")
	@Operation(summary = "API - 更新火車車次", description = "更新火車車次。")
	public ResponseEntity<TrainUpdatedResource> update(
			@Parameter(description = "火車車次資訊") @RequestBody UpdateTrainResource resource) {
		// DTO 轉換
		UpdateTrainCommand command = BaseDataTransformer.transformData(resource, UpdateTrainCommand.class);
		trainCommandService.updateTrain(command);
		return new ResponseEntity<>(new TrainUpdatedResource("200", "更新車次成功"), HttpStatus.OK);
	}

	/**
	 * 取得該火車車次的資訊
	 * 
	 * @param trainNo
	 * @return 該火車車次的停靠站資訊
	 */
	@Valid
	@GetMapping("/{trainNo}/stops")
	@Operation(summary = "API - 取得該火車車次的資訊", description = "取得該火車車次的資訊。")
	public ResponseEntity<TrainQueriedResource> query(
			@Parameter(description = "火車車次") @Valid @PathVariable @Min(value = 1, message = "車次必須為正整數") Integer trainNo) {
		return new ResponseEntity<>(BaseDataTransformer.transformData(trainQueryService.queryTrainData(trainNo),
				TrainQueriedResource.class), HttpStatus.OK);
	}

	/**
	 * 查詢符合條件的火車資訊(訂票查詢)
	 * 
	 * @param trainNo    車次
	 * @param trainKind  車種
	 * @param fromStop   起站
	 * @param toStop     迄站
	 * @param ticketType 車票種類
	 * @param takeDate   出發日期
	 * @param time       出發時間
	 * @return 該火車車次的停靠站資訊
	 */
	@GetMapping("")
	@Operation(summary = "API - 查詢符合條件的火車資訊(訂票查詢)", description = "查詢符合條件的火車資訊(訂票查詢)。")
	public ResponseEntity<List<TrainDetailQueriedResource>> getTrainInfo(
			@Parameter(description = "車次") @RequestParam(required = false) Integer trainNo,
			@Parameter(description = "車種") @RequestParam(required = false) String trainKind,
			@Parameter(description = "起站") @RequestParam String fromStop,
			@Parameter(description = "迄站") @RequestParam String toStop,
			@Parameter(description = "車票種類") @RequestParam String ticketType,
			@Parameter(description = "搭乘日期 (yyyy-mm-dd)") @Valid @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}") String takeDate,
			@Parameter(description = "搭乘時間 (hh:mm)") @RequestParam String time) {
		GetTrainQuery command = new GetTrainQuery(trainNo, trainKind, fromStop, toStop, takeDate, time,
				ticketType);
		return new ResponseEntity<>(BaseDataTransformer.transformData(trainQueryService.queryTrainInfo(command),
				TrainDetailQueriedResource.class), HttpStatus.OK);
	}

	/**
	 * 查詢符合條件的火車資訊
	 * 
	 * @param trainNo   車次
	 * @param trainKind 車種
	 * @param fromStop  起站
	 * @param toStop    迄站
	 * @param takeDate  出發日期
	 * @param time      出發時間
	 * @return 該火車車次的停靠站資訊
	 */
	@Valid
	@GetMapping("/summary")
	@Operation(summary = "API - 查詢符合條件的火車資訊", description = "查詢符合條件的火車資訊。")
	public ResponseEntity<List<TrainSummaryQueriedResource>> queryTrainSummary(
			@Parameter(description = "車次") @RequestParam(required = false) Integer trainNo,
			@Parameter(description = "車種") @RequestParam(required = false) String trainKind,
			@Parameter(description = "起站") @RequestParam String fromStop,
			@Parameter(description = "迄站") @RequestParam String toStop,
			@Parameter(description = "搭乘日期 (yyyy-mm-dd)") @Valid @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}") String takeDate,
			@Parameter(description = "搭乘時間 (hh:mm)") @RequestParam String time) {
		SummaryTrainQuery query = new SummaryTrainQuery(trainNo, trainKind, fromStop, toStop, takeDate,
				time);
		return new ResponseEntity<>(BaseDataTransformer.transformData(trainQueryService.summary(query),
				TrainSummaryQueriedResource.class), HttpStatus.OK);
	}

	/**
	 * 上傳車次資料
	 * 
	 * @param mapping
	 * @param sheetMapping
	 * @param file
	 * 
	 * @return ResponseEntity<TrainUploadedResource>
	 */
	@Operation(summary = "API - 上傳車次資料", description = "上傳車次資料。")
	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<TrainUploadedResource> upload(
			@Parameter(description = "Column Mapping", required = true) @RequestParam(name = "mapping", required = true) String mapping,
			@Parameter(description = "Sheet Name Mapping", required = true) @RequestParam(name = "sheetMapping", required = true) String sheetMapping,
			@Parameter(description = "要上傳的文件", required = true) @RequestPart(name = "file", required = true) MultipartFile file) {
		try {
			trainCommandService.upload(mapping, sheetMapping, file);
		} catch (IOException e) {
			log.error("發生錯誤，上傳失敗", e);
			throw new ValidationException("VALIDATE_FAILED", "發生錯誤，上傳失敗"); // 拋出例外
		}
		return new ResponseEntity<>(new TrainUploadedResource("200", "上傳成功"), HttpStatus.OK);
	}

	/**
	 * 下載火車時刻表
	 * 
	 * @param resource
	 * @return 成功訊息
	 */
	@Operation(summary = "API - 下載火車時刻表", description = "下載火車時刻表。")
	@GetMapping(value = "/download/timetable", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public ResponseEntity<Resource> downloadTimetable(@Parameter(description = "起站") @RequestParam String fromStop,
			@Parameter(description = "迄站") @RequestParam String toStop) {
		// DTO 轉換
		SummaryTrainQuery command = new SummaryTrainQuery();
		command.setFromStop(fromStop);
		command.setToStop(toStop);
		ByteArrayResource resource = trainCommandService.downloadTimetable(command);

		// 設置檔名進 Header
//		 String encodedFileName = UriUtils.encode(fileName, StandardCharsets.UTF_8);
		// 待確認是否加密
		ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
				.filename("火車時刻表.pdf", StandardCharsets.UTF_8).build();
		HttpHeaders respHeaders = new HttpHeaders();
		respHeaders.setContentDisposition(contentDisposition);
		return new ResponseEntity<>(resource, respHeaders, HttpStatus.OK);

	}
}
