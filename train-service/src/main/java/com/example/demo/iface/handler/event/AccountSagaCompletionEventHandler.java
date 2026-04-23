package com.example.demo.iface.handler.event;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.example.demo.application.service.NotificationApplicationService;
import com.example.demo.base.iface.handler.BaseEventHandler;
import com.example.demo.domain.account.outbound.AccountRegistrationCompletedEvent;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor
@RabbitListener(queues = "${rabbitmq.account.saga.completion}")
public class AccountSagaCompletionEventHandler extends BaseEventHandler {

	 private NotificationApplicationService notificationService;

	@RabbitHandler
	public void handle(AccountRegistrationCompletedEvent event) {
		log.info("SAGA 終點 - 開戶成功確認：{}", event.getTargetId());

		// 1. 冪等性檢查：確保寄送歡迎信等動作不會重複執行
		if (!this.checkEventIdempotency(event)) {
			return;
		}

		log.info("恭喜使用者 {} 註冊成功！準備寄送歡迎信至 {}", event.getUsername(), event.getEmail());

		// 2. 寄送開戶成功歡迎信
        notificationService.sendWelcomeEmail(event.getEmail(), event.getUsername());

		// 3. 記錄成功指標 (Metrics)
		// metrics.recordRegistrationSuccess();

		log.info("帳戶 {} 的開戶成功後續處理（通知）已完成。", event.getTargetId());
	}
}