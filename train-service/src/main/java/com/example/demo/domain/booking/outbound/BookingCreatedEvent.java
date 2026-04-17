package com.example.demo.domain.booking.outbound;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.example.demo.base.infra.annotation.EventTopic;
import com.example.demo.base.shared.event.BaseEvent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@EventTopic("${rabbitmq.booking.saga.start}")
public class BookingCreatedEvent extends BaseEvent {

	private String method; // 付款方式

	private Long carNo; // 車廂編號

	private String seatNo; // 座號

	private LocalDate takeDate; // 乘車日期

	private BigDecimal price; // 價格

}