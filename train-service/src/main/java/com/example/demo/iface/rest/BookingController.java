package com.example.demo.iface.rest;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.application.shared.dto.TicketBookedData;
import com.example.demo.domain.booking.command.BookTicketCommand;
import com.example.demo.domain.booking.command.CancelTicketBookingCommand;
import com.example.demo.domain.booking.command.CheckInTicketBookingCommand;
import com.example.demo.domain.share.dto.BookingQueriedView;
import com.example.demo.iface.dto.req.BookTicketResource;
import com.example.demo.iface.dto.req.CancelTicketBookingResource;
import com.example.demo.iface.dto.req.CheckInTicketResource;
import com.example.demo.iface.dto.res.BookingCancelledResource;
import com.example.demo.iface.dto.res.BookingCheckedInResource;
import com.example.demo.iface.dto.res.BookingQueriedResource;
import com.example.demo.iface.dto.res.TicketBookedResource;
import com.example.demo.service.BookQueryService;
import com.example.demo.service.BookingCommandService;
import com.example.demo.util.BaseDataTransformer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/booking")
@Tag(name = "Booking API", description = "進行車票預訂領域相關動作")
public class BookingController {

	private BookQueryService bookQueryService;
	private BookingCommandService ticketCommandService;

	/**
	 * 查詢該使用者的訂票資訊
	 * 
	 * @param username
	 */
	@GetMapping("/{username}")
	@Operation(summary = "API - 查詢該使用者的訂票資訊", description = "查詢該使用者的訂票資訊。")
	public ResponseEntity<BookingQueriedResource> queryBooking(
			@Parameter(description = "使用者帳號") @PathVariable String username) {
		BookingQueriedView bookQueriedData = bookQueryService.queryBooking(username);
		return new ResponseEntity<>(BaseDataTransformer.transformData(bookQueriedData, BookingQueriedResource.class),
				HttpStatus.OK);
	}

	/**
	 * 預定車票資料
	 * 
	 * @param resource
	 * @return ResponseEntity
	 */
	@PostMapping("")
	@Operation(summary = "API - 預定車票座位資料", description = "預定車票座位資料。")
	public ResponseEntity<TicketBookedResource> bookTicket(
			@Parameter(description = "車票預定資訊") @RequestBody BookTicketResource resource) {
		BookTicketCommand command = BaseDataTransformer.transformData(resource, BookTicketCommand.class);
		TicketBookedData booked = ticketCommandService.bookTicket(command);
		return new ResponseEntity<>(new TicketBookedResource("200", "預定成功", booked.getUuid()), HttpStatus.OK);
	}

	/**
	 * 進行車票座位 Check in 動作
	 * 
	 * @param resource
	 * @return ResponseEntity
	 */
	@PostMapping("/check-in")
	@Operation(summary = "API - 進行車票座位 Check in 動作", description = "進行車票座位 Check in 動作。")
	public ResponseEntity<BookingCheckedInResource> bookTicket(
			@Parameter(description = "車票 Check IN 資訊") @RequestBody CheckInTicketResource resource) {
		CheckInTicketBookingCommand command = BaseDataTransformer.transformData(resource,
				CheckInTicketBookingCommand.class);
		return new ResponseEntity<>(BaseDataTransformer.transformData(ticketCommandService.checkInBooking(command),
				BookingCheckedInResource.class), HttpStatus.OK);
	}

	/**
	 * 進行取消車票訂位動作
	 * 
	 * @param resource
	 * @return ResponseEntity
	 */
	@PostMapping("/cancel")
	@Operation(summary = "API - 進行取消車票訂位動作", description = "進行取消車票訂位動作。")
	public ResponseEntity<BookingCancelledResource> cancelBooking(
			@Parameter(description = "車票退票資訊") @RequestBody CancelTicketBookingResource resource) {

		// 1. 生成這次取消流程的 Saga ID
		String sagaTxId = UUID.randomUUID().toString();
		log.info("API 接收到取消訂票請求 - TX_ID: {}, BookingUuid: {}", sagaTxId, resource.getUuid());

		CancelTicketBookingCommand command = BaseDataTransformer.transformData(resource,
				CancelTicketBookingCommand.class);

		// 2. 執行 Service (內部會發動 Saga)
		ticketCommandService.cancelBooking(command, sagaTxId);

		return new ResponseEntity<>(new BookingCancelledResource("200", "取消成功"), HttpStatus.OK);

	}

}
