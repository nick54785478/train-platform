package com.example.demo.base.iface.handler;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.application.port.EventTopicResolverPort;
import com.example.demo.base.application.port.DataTransformerPort;
import com.example.demo.base.application.port.EventIdempotenceHandlerPort;
import com.example.demo.base.application.port.EventPublisherPort;
import com.example.demo.base.domain.aggregate.DomainEvent;
import com.example.demo.base.infra.persistence.EventLogRepository;
import com.example.demo.base.infra.persistence.EventSourceRepository;
import com.example.demo.base.shared.command.PublishEventCommand;
import com.example.demo.base.shared.entity.EventLog;
import com.example.demo.base.shared.enums.EventLogSendQueueStatus;
import com.example.demo.base.shared.event.BaseEvent;
import com.example.demo.util.JsonParseUtil;

/**
 * Base Event Handler
 */
@Component
public class BaseEventHandler {

	@Autowired
	protected EventPublisherPort eventPublisher;
	@Autowired
	protected DataTransformerPort dataTransformer;
	@Autowired
	protected EventLogRepository eventLogRepository;
	@Autowired
	protected EventTopicResolverPort eventTopicResolver;
	@Autowired
	protected EventSourceRepository eventSourceRepository;
	@Autowired
	protected EventIdempotenceHandlerPort eventIdempotenceHandler;

	/**
	 * 檢查冪等
	 * 
	 * @param event
	 * @return boolean
	 */
	public boolean checkEventIdempotency(BaseEvent event) {
		return eventIdempotenceHandler.handleIdempotency(event);
	}

	/**
	 * 呼叫 BaseDataTransformer 進行資料轉換
	 * 
	 * @param <T>
	 * @param target 目標物件
	 * @param clazz  欲轉換的型別
	 * @return 轉換後的物件
	 */
	public <T> T transformData(Object target, Class<T> clazz) {
		return dataTransformer.transform(target, clazz);
	}

	/**
	 * 呼叫 BaseDataTransformer 進行資料轉換
	 * 
	 * @param <S,    T>
	 * @param target 目標物件列表
	 * @param clazz  欲轉換的型別
	 * @return 轉換後的物件列表
	 */
	public <S, T> List<T> transformData(List<S> target, Class<T> clazz) {
		return dataTransformer.transformList(target, clazz);
	}

	/**
	 * 發布事件 (Event)
	 * 
	 * @param event 事件
	 */
	public void publishEvent(BaseEvent event) {

		String topic = eventTopicResolver.resolve(event);

		PublishEventCommand<BaseEvent> publishCommand = PublishEventCommand.<BaseEvent>builder().event(event)
				.topic(topic).build();
		eventPublisher.publish(publishCommand);
	}

	/**
	 * 建立事件紀錄 (EventLog) 實體
	 * <p>
	 * 負責將業務事件包裝成標準化的 {@link EventLog} 格式，準備存入資料庫。 此階段狀態會設為 {@code INITIAL}。
	 * </p>
	 * 
	 * @param topic 訊息發送的 Topic 通道
	 * @param event 業務事件主體 (繼承自 BaseEvent)
	 * @return 已填充資料的 EventLog 實體
	 */
	public EventLog generateEventLog(String topic, DomainEvent event) {

		// 構建 EventLog 實體並初始化狀態
		return EventLog.builder().uuid(UUID.randomUUID().toString()) // 產生唯一識別碼
				.topic(topic) // 設定目標 Topic
				.userId("SYSTEM") // 預設操作者 (可視需求調整為 Session 使用者)
				.className(event.getClass().getName()) // 記錄原始事件類別全名，供後續反射還原使用
				.targetId(event.getTargetId()) // 記錄關聯的業務 ID (例如訂單編號)
				.txId(event.getEventTxId()) // 關聯分散式事務 ID 或追蹤碼
				.body(JsonParseUtil.serialize(event)) // 將事件內容序列化為 JSON 字串
				.status(EventLogSendQueueStatus.INITIAL) // 初始化狀態為待處理
				.build();
	}

}
