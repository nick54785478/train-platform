package com.example.demo.domain.account.outbound;

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
@EventTopic("${rabbitmq.account.saga.failure}")
public class AccountRegistrationFailedEvent extends BaseEvent {

	private String reason; // 失敗原因 (例如：儲值超時、系統異常)

	private String email; // 用於通知管理員處理

}
