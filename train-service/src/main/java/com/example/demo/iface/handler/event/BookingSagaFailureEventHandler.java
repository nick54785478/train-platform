package com.example.demo.iface.handler.event;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.example.demo.base.iface.handler.BaseEventHandler;
import com.example.demo.domain.booking.outbound.BookingExecutedFailedEvent;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RabbitListener(queues = "${rabbitmq.booking.saga.failure}")
public class BookingSagaFailureEventHandler extends BaseEventHandler {

	@RabbitHandler
	public void fail(BookingExecutedFailedEvent event) {
		log.error("Saga 流程最終確認失敗：訂單 {}", event.getTargetId());

		// 1. 寄送通知給使用者
//		notificationService.sendFailureEmail(event.getTargetId(), event.getReason());

		// 2. 寫入審計日誌 (Audit Log) 或埋點監控
//		metricsService.recordSagaFailure();
	}
}
