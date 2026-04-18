package com.example.demo.iface.handler.event;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.example.demo.application.service.NotificationService;
import com.example.demo.base.iface.handler.BaseEventHandler;
import com.example.demo.domain.account.outbound.AccountRegistrationFailedEvent;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor
@RabbitListener(queues = "${rabbitmq.account.saga.failure}")
public class AccountSagaFailureEventHandler extends BaseEventHandler {

	private NotificationService notificationService;

	@RabbitHandler
	public void handle(AccountRegistrationFailedEvent event) {

		log.error("SAGA 流程結束 [失敗] - 帳戶 ID: {}, 原因: {}", event.getTargetId(), event.getReason());


		// 1. 寄送通知給使用者 (使用 event 帶過來的 email，免去二次查詢)
		log.info("準備寄送失敗通知至：{}", event.getEmail());
		notificationService.sendAccountRegistrationFailureAlert(event.getEmail(), event.getReason());

		// 2. 記錄審計日誌或監控指標 (Metrics)
		// metrics.increment("saga.account.registration.failure");

		log.info("帳戶 {} 的開戶失敗後續處理完成。", event.getTargetId());
	}
}
