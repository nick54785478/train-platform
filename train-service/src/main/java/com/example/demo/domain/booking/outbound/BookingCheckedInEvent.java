package com.example.demo.domain.booking.outbound;

import java.time.LocalDate;

import com.example.demo.base.domain.aggregate.DomainEvent;
import com.example.demo.base.infra.annotation.EventTopic;

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
@EventTopic("${rabbitmq.checkin-seat-topic-queue.name}")
public class BookingCheckedInEvent extends DomainEvent {

	private String action; // 動作

	private Long carNo; // 車廂編號

	private String seatNo; // 座號

	private LocalDate takeDate; // 乘車日期

}
