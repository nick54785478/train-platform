package com.example.demo.domain.account.outbound;

import com.example.demo.base.domain.aggregate.DomainEvent;
import com.example.demo.base.infra.annotation.EventTopic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Saga 終點事件：帳戶註冊並初始儲值成功
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@EventTopic("${rabbitmq.account.saga.completion}")
public class AccountRegistrationCompletedEvent extends DomainEvent {

	private String username;

	private String email;

}
