package com.example.demo.iface.handler.event;

import java.io.IOException;
import java.util.Objects;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.example.demo.application.service.MoneyAccountCommandService;
import com.example.demo.base.iface.handler.BaseEventHandler;
import com.example.demo.domain.account.command.DepositMoneyCommand;
import com.example.demo.domain.account.outbound.AccountRegisteredEvent;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RabbitListener(queues = "${rabbitmq.account.saga.start}")
public class AccountSagaStartEventHandler extends BaseEventHandler {

	private final MoneyAccountCommandService moneyAccountCommandService;

	public AccountSagaStartEventHandler(MoneyAccountCommandService moneyAccountCommandService) {
		this.moneyAccountCommandService = moneyAccountCommandService;
	}

	@RabbitHandler
	public void handle(AccountRegisteredEvent event) throws IOException {
		log.info("Account Saga Start Topic Queue -- 接收到消息： {}", event);

		if (Objects.isNull(event)) {
			log.error("Consumer 接收到的 Message 有問題, 內容: {}", event);
			return;
		}

		// 冪等機制，防止重覆消費所帶來的副作用
		if (!this.checkEventIdempotency(event)) {
			log.warn("Consume repeated: {}", event);
			return;
		}

		// 呼叫 Application Service 進行儲值處理
		DepositMoneyCommand command = DepositMoneyCommand.builder().uuid(event.getTargetId()).money(event.getMoney())
				.build();
		moneyAccountCommandService.depositAndActivate(command, event.getEventTxId());

	}
}
