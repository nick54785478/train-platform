package com.example.demo.iface.handler.event;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.example.demo.base.iface.handler.BaseEventHandler;
import com.example.demo.domain.account.outbound.FareChargedFailedEvent;
import com.example.demo.domain.seat.command.CancelSeatCompensationCommand;
import com.example.demo.domain.seat.outbound.SeatBookedFailedEvent;
import com.example.demo.domain.seat.outbound.SeatCancelledEvent;
import com.example.demo.service.BookingCommandService;
import com.example.demo.service.SeatCommandService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor
@RabbitListener(queues = "${rabbitmq.booking.saga.compensation}")
public class BookingSagaCompensationEventHandler extends BaseEventHandler {

	private SeatCommandService seatCommandService;
	private BookingCommandService bookingCommandService;

	/**
	 * 處理：扣款失敗後的補償 (需要取消座位)
	 */
	@RabbitHandler
	public void handleFareChargedFailedEvent(FareChargedFailedEvent event) {
		log.warn("Saga Compensation Topic Queue -- 接收到補償要求： {}", event);
		log.warn("進行補償機制，取消 Seat Booked");

		if (!checkEventIdempotency(event)) {
			return;
		}
		CancelSeatCompensationCommand command = CancelSeatCompensationCommand.builder().bookingUuid(event.getTargetId())
				.reason(event.getReason()).accountUuid(event.getAccountUuid()).build();
		// 執行取消座位 + 標記訂單失敗
		seatCommandService.cancelSeat(command, event.getEventTxId());
	}

	/**
	 * 處理：劃位本身失敗後的補償 (只需標記訂單失敗)
	 */
	@RabbitHandler
	public void handleSeatBookedFailedEvent(SeatBookedFailedEvent event) {
		log.warn("SAGA 補償 - 偵測到劃位階段失敗：{}", event);

		if (!checkEventIdempotency(event)) {
			return;
		}

		// 既然劃位失敗了，代表資料庫可能沒存到座位，直接將訂單改為 FAILED
		bookingCommandService.fail(event.getTargetId(), event.getEventTxId(), event.getReason());

		log.info("SAGA 補償完成 (劃位失敗路徑)：訂單 {}", event.getTargetId());
	}

	/**
	 * 處理：接收補償動作執行完產生的結果事件 
	 * <p>目的：防止 Spring AMQP 因為找不到處理器而拋出 NoSuchMethodException 並丟棄訊息</p>
	 */
	@RabbitHandler
	public void handleSeatCancelledEvent(SeatCancelledEvent event) {
		log.info("Saga 補償路徑執行完畢。訂單 {} 的座位已完成釋放。", event.getBookingUuid());
		bookingCommandService.fail(event.getBookingUuid(), event.getEventTxId(), event.getReason());
	}
}
