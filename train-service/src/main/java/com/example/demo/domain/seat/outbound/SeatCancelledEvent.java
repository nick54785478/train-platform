package com.example.demo.domain.seat.outbound;

import com.example.demo.base.domain.aggregate.DomainEvent;
import com.example.demo.base.infra.annotation.EventTopic;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@EventTopic("${rabbitmq.booking.saga.compensation}")
public class SeatCancelledEvent extends DomainEvent {

	private String bookingUuid;

	private String reason;
}
