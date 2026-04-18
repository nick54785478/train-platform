package com.example.demo.iface.handler.event;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.example.demo.base.iface.handler.BaseEventHandler;
import com.example.demo.domain.booking.outbound.BookingExecutedFailedEvent;
import com.example.demo.service.NotificationService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor
@RabbitListener(queues = "${rabbitmq.booking.saga.failure}")
public class BookingSagaFailureEventHandler extends BaseEventHandler {

	private NotificationService notificationService;
	
	@RabbitHandler
	public void fail(BookingExecutedFailedEvent event) {
		log.error("Saga 流程最終確認失敗：訂單 {}", event.getTargetId());

		// 1. 寄送通知給使用者
		notificationService.sendBookingFailureAlert(event.getTargetId(), event.getReason(), event.getEmail());

		// 2. 寫入審計日誌 (Audit Log) 或埋點監控
//		metricsService.recordSagaFailure();
	}
}
