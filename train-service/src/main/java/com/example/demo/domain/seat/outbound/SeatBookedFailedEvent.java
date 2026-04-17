package com.example.demo.domain.seat.outbound;

import com.example.demo.base.infra.annotation.EventTopic;
import com.example.demo.base.shared.event.BaseEvent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@EventTopic("${rabbitmq.booking.saga.compensation}")
public class SeatBookedFailedEvent  extends BaseEvent {
	 
	private String reason;
}
