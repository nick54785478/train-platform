package com.example.demo.domain.account.outbound;

import java.math.BigDecimal;

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
@EventTopic("${rabbitmq.topic.account.transaction}")
public class MoneyDepositedEvent extends BaseEvent {

	private BigDecimal amount; // 金額

	private BigDecimal balanceAfter; // 儲值後的餘額（選擇性攜帶）
}
