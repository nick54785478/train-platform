//package com.example.demo.domain.account.outbound;
//
//import java.math.BigDecimal;
//
//import com.example.demo.base.infra.annotation.EventTopic;
//import com.example.demo.base.shared.event.BaseEvent;
//
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.EqualsAndHashCode;
//import lombok.NoArgsConstructor;
//import lombok.experimental.SuperBuilder;
//
//@Data
//@SuperBuilder
//@AllArgsConstructor
//@NoArgsConstructor
//@EqualsAndHashCode(callSuper = true)
//@EventTopic("${rabbitmq.acount-tx-topic-queue.name}")
//public class AccountTxEvent extends BaseEvent {
//
//	private BigDecimal money; // 金額
//}
