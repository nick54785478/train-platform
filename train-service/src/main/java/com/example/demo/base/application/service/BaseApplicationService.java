package com.example.demo.base.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.base.application.port.DataTransformerPort;
import com.example.demo.base.application.port.EventPublisherPort;
import com.example.demo.base.application.port.EventTopicResolverPort;
import com.example.demo.base.infra.persistence.EventLogRepository;
import com.example.demo.base.shared.entity.EventLog;
import com.example.demo.base.shared.enums.EventLogSendQueueStatus;
import com.example.demo.base.shared.event.BaseEvent;
import com.example.demo.util.JsonParseUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 基礎應用程式服務類別 (Base Application Service)
 * 
 * <pre>
 * 提供子類別通用的工具方法，核心功能包含： 
 * 1. 透過 {@link DataTransformerPort} 進行物件型別轉換 (如: Entity 轉 DTO)。 
 * 2. 實作 Outbox Pattern 的核心邏輯，將 Domain Events 轉換為 {@link EventLog} 並持久化至資料庫。
 * </pre>
 * 
 */
@Slf4j
@Service
public abstract class BaseApplicationService {

	/**
	 * 訊息佇列發送埠，用於後續發送事件至 MQ
	 */
	@Autowired
	protected EventPublisherPort eventPublisher;

	/**
	 * 事件紀錄儲存庫，對應 Outbox Table 的資料存取
	 */
	@Autowired
	protected EventLogRepository eventLogRepository;

	/**
	 * 事件主題解析器，用於根據事件內容動態決定 MQ Topic
	 */
	@Autowired
	private EventTopicResolverPort eventTopicResolver;

	/**
	 * 資料轉換組件，處理物件對映與屬性複製
	 */
	@Autowired
	private DataTransformerPort dataTransformer;

	/**
	 * 單一物件資料轉換
	 * <p>
	 * 封裝底層轉換邏輯，常用於 Entity 與 DTO 之間的轉換。
	 * </p>
	 * * @param <T> 目標物件型別
	 * 
	 * @param target 來源物件 (待轉換的物件)
	 * @param clazz  目標物件的 Class 型別
	 * @return 轉換後的目標物件實例
	 */
	public <T> T transformData(Object target, Class<T> clazz) {
		return dataTransformer.transform(target, clazz);
	}

	/**
	 * 列表物件資料轉換
	 * <p>
	 * 批次處理集合資料的轉換邏輯。
	 * </p>
	 * * @param <S> 來源物件型別
	 * 
	 * @param <T>    目標物件型別
	 * @param target 來源物件列表
	 * @param clazz  目標物件的 Class 型別
	 * @return 轉換後的目標物件列表
	 */
	public <S, T> List<T> transformData(List<S> target, Class<T> clazz) {
		return dataTransformer.transformList(target, clazz);
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
	public EventLog generateEventLog(String topic, BaseEvent event) {
		System.out.println("type: " + event.getClass().getSimpleName() + ", eventTxId:" + event.getEventTxId());

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

	/**
	 * 執行 Outbox 核心作業：持久化事件至資料庫
	 * <p>
	 * <b>重要設計原則：</b><br>
	 * 1. 此方法應在一個資料庫事務 (Transaction) 中執行。<br>
	 * 2. 此處「不發送」MQ，僅寫入 DB。目的是保證業務數據與事件數據的一致性。<br>
	 * 3. 實際的 MQ 發送應由獨立的定時任務 (Scheduler) 掃描 DB 後進行。
	 * </p>
	 * 
	 * @param domainEvents 待處理的領域事件 (Domain Events) 列表
	 */
	public void saveDomainEventsToOutbox(List<BaseEvent> domainEvents) {

		domainEvents.forEach(event -> {

			// 1. 透過解析器動態取得該事件對應的 MQ Topic
			String topic = eventTopicResolver.resolve(event);

			// 2. 封裝成 EventLog 資料結構
			EventLog eventLog = this.generateEventLog(topic, event);

			// 3. 寫入 Outbox 資料表 (EventLog Repository)
			eventLogRepository.save(eventLog);
		});
	}

	/**
	 * 強制寫入 Outbox (獨立交易)
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void forceSaveDomainEvent(BaseEvent event) {
		log.info("獨立交易：寫入失敗事件至 Outbox，eventTxId:{}", event.getEventTxId());
		this.saveDomainEventsToOutbox(List.of(event));
	}

}
