package com.example.demo.iface.handler.event;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.example.demo.application.service.BookingCommandService;
import com.example.demo.application.service.NotificationApplicationService;
import com.example.demo.application.shared.dto.BookingCompletedData;
import com.example.demo.base.iface.handler.BaseEventHandler;
import com.example.demo.domain.account.outbound.FareChargedEvent;
import com.example.demo.domain.booking.command.CompleteBookingCommand;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RabbitListener(queues = "${rabbitmq.booking.saga.completion}")
public class BookingSagaCompletionEventHandler extends BaseEventHandler {

	private final NotificationApplicationService notificationService; // 注入通知服務
	private final BookingCommandService bookingCommandService;

	public BookingSagaCompletionEventHandler(NotificationApplicationService notificationService,
			BookingCommandService bookingCommandService) {
		this.notificationService = notificationService;
		this.bookingCommandService = bookingCommandService;
	}

	/**
	 * SAGA 流程結束，訂單完成
	 */
	@RabbitHandler
	public void complete(FareChargedEvent event) {
		log.warn(" SAGA 流程結束，訂單完成：訂單 ID {}", event.getTargetId());

		if (!checkEventIdempotency(event)) {
			return;
		}

		// 1. 執行業務邏輯：變更訂單狀態為 COMPLETED
		CompleteBookingCommand command = CompleteBookingCommand.builder().bookingUuid(event.getTargetId()).build();

		// 建議讓這個 method 回傳 Booking 的資料物件，或是含有 Email 的 DTO
		BookingCompletedData bookingData = bookingCommandService.completeBooking(command);

		// 2. 寄送成功通知
		if (bookingData != null && bookingData.getEmail() != null) {
			log.info("準備寄送訂票成功信至：{}", bookingData.getEmail());
			notificationService.sendBookingSuccessEmail(bookingData.getEmail(), event.getTargetId());
		} else {
			log.warn("無法寄送確認信：找不到訂單 {} 的 Email 資訊", event.getTargetId());
		}
	}
}
