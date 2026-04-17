package com.example.demo.domain.account.outbound;

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
@EventTopic("${rabbitmq.account.saga.compensation}")
public class AccountDepositFailedEvent extends BaseEvent {

	private BigDecimal money; // 金額

	private String reason; // 失敗原因

}
