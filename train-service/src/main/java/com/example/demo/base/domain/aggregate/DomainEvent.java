package com.example.demo.base.domain.aggregate;

import com.example.demo.base.shared.event.BaseEvent;

import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Domain Event 基礎實體類，此類包含一些通用的欄位，如: 訊息識別符、目標代碼。
 */
@Data
@SuperBuilder
@MappedSuperclass
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DomainEvent extends BaseEvent {

	/**
	 * 事件的唯一代號
	 */
	protected String eventTxId;

}
