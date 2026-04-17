package com.example.demo.domain.seat.outbound;

import java.math.BigDecimal;

import com.example.demo.base.infra.annotation.EventTopic;
import com.example.demo.base.shared.event.BaseEvent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@EventTopic("${rabbitmq.booking.saga.process}")
public class SeatBookedEvent extends BaseEvent {
	
	private String bookingUuid; // Booking UUID

	private BigDecimal money; // 金額
}