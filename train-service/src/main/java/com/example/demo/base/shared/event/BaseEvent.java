package com.example.demo.base.shared.event;

import java.util.UUID;

import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Event 基礎實體類，此類包含一些通用的欄位，如: 訊息識別符、目標代碼。
 */
@Data
@SuperBuilder
@MappedSuperclass
@NoArgsConstructor
@AllArgsConstructor
public class BaseEvent {

	/**
	 * 該消息日誌的唯一識別符
	 */
	@Default
	protected String eventLogUuid = UUID.randomUUID().toString();

	/**
	 * targetId
	 */
	protected String targetId;

	/**
	 * 事件的唯一代號
	 */
	protected String eventTxId;

}
