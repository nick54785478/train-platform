package com.example.demo.domain.seat.outbound;

import java.math.BigDecimal;

import com.example.demo.base.domain.aggregate.DomainEvent;
import com.example.demo.base.infra.annotation.EventTopic;

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
@EventTopic("${rabbitmq.acount-tx-topic-queue.name}")
public class SeatCancelledByUserEvent extends DomainEvent {

	private BigDecimal refund; // 退款金額
}