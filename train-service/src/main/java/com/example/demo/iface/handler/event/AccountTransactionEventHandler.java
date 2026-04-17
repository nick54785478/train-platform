package com.example.demo.iface.handler.event;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.example.demo.base.iface.handler.BaseEventHandler;
import com.example.demo.domain.account.outbound.MoneyDepositedEvent;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RabbitListener(queues = "${rabbitmq.topic.account.transaction}")
public class AccountTransactionEventHandler extends BaseEventHandler {

	/**
	 * 這是「存完錢後」的終點處理 監聽的是 MoneyDepositedEvent (領域事件)
	 */
	@RabbitHandler
	public void handleDepositSuccess(MoneyDepositedEvent event) {
		log.info("SAGA 終點(日常) - 帳戶 {} 儲值成功，金額: {}。開始執行後續動作...", event.getTargetId(), event.getAmount());

		if (!this.checkEventIdempotency(event)) {
			return;
		}
		// 這裡做的是「存錢以外」的事情：
		// 1. 寄送儲值成功簡訊
		// 2. 增加會員積分
		// 3. 更新報表系統
		log.info("已寄送通知信至該使用者，TX_ID: {}", event.getEventTxId());
	}
}