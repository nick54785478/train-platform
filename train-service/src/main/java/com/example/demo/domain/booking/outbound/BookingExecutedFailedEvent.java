package com.example.demo.domain.booking.outbound;

import com.example.demo.base.domain.aggregate.DomainEvent;
import com.example.demo.base.infra.annotation.EventTopic;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@EventTopic("${rabbitmq.booking.saga.failure}")
public class BookingExecutedFailedEvent extends DomainEvent {

	private String email; // 訂票者信箱

	private String reason; // 失敗原因
}
