package com.example.demo.domain.account.outbound;

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
@EventTopic("${rabbitmq.booking.saga.completion}")
public class FareChargedEvent extends DomainEvent {

}
