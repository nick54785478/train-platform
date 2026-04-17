package com.example.demo.iface.handler.event;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.example.demo.base.iface.handler.BaseEventHandler;
import com.example.demo.domain.account.outbound.FareChargedEvent;
import com.example.demo.domain.booking.command.CompleteBookingCommand;
import com.example.demo.service.BookingCommandService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RabbitListener(queues = "${rabbitmq.booking.saga.completion}")
public class BookingSagaCompletionEventHandler extends BaseEventHandler {

	private final BookingCommandService bookingCommandService;

	public BookingSagaCompletionEventHandler(BookingCommandService bookingCommandService) {
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
		CompleteBookingCommand command = CompleteBookingCommand.builder().bookingUuid(event.getTargetId()).build();
		bookingCommandService.completeBooking(command);
	}
}
