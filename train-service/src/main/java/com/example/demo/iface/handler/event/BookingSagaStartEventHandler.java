package com.example.demo.iface.handler.event;

import java.io.IOException;
import java.util.Objects;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.example.demo.base.iface.handler.BaseEventHandler;
import com.example.demo.domain.booking.outbound.BookingCreatedEvent;
import com.example.demo.domain.seat.command.BookSeatCommand;
import com.example.demo.infra.repository.TicketBookingRepository;
import com.example.demo.service.SeatCommandService;
import com.rabbitmq.client.Channel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RabbitListener(queues = "${rabbitmq.booking.saga.start}")
public class BookingSagaStartEventHandler extends BaseEventHandler {

	private SeatCommandService seatCommandService;
	private TicketBookingRepository ticketBookingRepository;

	public BookingSagaStartEventHandler(TicketBookingRepository ticketBookingRepository,
			SeatCommandService seatCommandService) {
		this.ticketBookingRepository = ticketBookingRepository;
		this.seatCommandService = seatCommandService;
	}

	@RabbitHandler
	public void handle(BookingCreatedEvent event, Channel channel, Message message) throws IOException {
		log.info("Booking Saga Start Topic Queue -- 接收到消息： {}", event);

		if (Objects.isNull(event)) {
			log.error("Consumer 接收到的 Message 有問題, 內容:{}", event);
			return;
		}

		// 冪等機制，防止重覆消費所帶來的副作用
		if (!this.checkEventIdempotency(event)) {
			log.warn("Consume repeated: {}", event);
			return;
		}

		ticketBookingRepository.findById(event.getTargetId()).ifPresent(booking -> {

			BookSeatCommand command = BookSeatCommand.builder().ticketUuid(booking.getTicketUuid())
					.trainUuid(booking.getTrainUuid()).bookingUuid(booking.getUuid()).takeDate(event.getTakeDate())
					.seatNo(event.getSeatNo()).carNo(event.getCarNo()).build();
			// 進行劃位動作
			seatCommandService.bookSeat(command, event.getEventTxId(), event.getMethod(), event.getPrice());

		});
	}


}
