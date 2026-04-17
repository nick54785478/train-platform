package com.example.demo.iface.handler.event;

import java.util.Objects;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.example.demo.base.iface.handler.BaseEventHandler;
import com.example.demo.domain.account.command.ChargeFareCommand;
import com.example.demo.domain.seat.outbound.SeatBookedEvent;
import com.example.demo.service.MoneyAccountCommandService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RabbitListener(queues = "${rabbitmq.booking.saga.process}")
public class BookingSagaProcessEventHandler extends BaseEventHandler {

	private MoneyAccountCommandService moneyAccountCommandService;

	public BookingSagaProcessEventHandler(MoneyAccountCommandService moneyAccountCommandService) {
		this.moneyAccountCommandService = moneyAccountCommandService;
	}

	/**
	 * 訂位完成後進行扣款
	 * 
	 * @param {@link SeatBookedEvent}
	 */
	@RabbitHandler
	public void handle(SeatBookedEvent event) {
		log.info("Saga Process Topic Queue -- 接收到消息： {}", event);
		if (Objects.isNull(event)) {
			log.error("Consumer 接收到的 Message 有問題, 內容: {}", event);
			return;
		}

		// 冪等機制，防止重覆消費所帶來的副作用
		if (!this.checkEventIdempotency(event)) {
			log.warn("Consume repeated: {}", event);
			return;
		}

		ChargeFareCommand command = ChargeFareCommand.builder().uuid(event.getTargetId())
				.bookingUuid(event.getBookingUuid()).money(event.getMoney()).build();
		moneyAccountCommandService.charge(command, event.getEventTxId());
	}
	
	

}
