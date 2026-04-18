package com.example.demo.base.domain.service;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.base.infra.context.ContextHolder;
import com.example.demo.base.infra.persistence.EventLogRepository;
import com.example.demo.base.infra.persistence.EventSourceRepository;
import com.example.demo.base.shared.entity.EventLog;
import com.example.demo.base.shared.entity.EventSource;
import com.example.demo.base.shared.enums.StatusCode;
import com.example.demo.base.shared.event.BaseEvent;
import com.example.demo.util.BaseDataTransformer;
import com.example.demo.util.JsonParseUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * Base Domain Service
 */
@Slf4j
@Service
public abstract class BaseDomainService {

	@Autowired
	protected EventLogRepository eventLogRepository;
	@Autowired
	protected EventSourceRepository eventSourceRepository;

	/**
	 * 呼叫 BaseDataTransformer 進行資料轉換
	 * 
	 * @param <T>
	 * 
	 * @param target 目標物件
	 * @param clazz  欲轉換的型別
	 * @return 轉換後的物件
	 */
	public <T> T transformAggregate(Object target, Class<T> clazz) {
		return BaseDataTransformer.transformData(target, clazz);
	}

	/**
	 * 呼叫 BaseDataTransformer 進行資料轉換
	 * 
	 * @param <S,    T>
	 * 
	 * @param target 目標物件列表
	 * @param clazz  欲轉換的型別
	 * @return 轉換後的物件列表
	 */
	public <S, T> List<T> transformAggregate(List<S> target, Class<T> clazz) {
		return BaseDataTransformer.transformData(target, clazz);
	}

	/**
	 * 紀錄 Event Source
	 * 
	 * @param eventStreamId Event Stream Id (格式: Aggregate Type-Aggregate ID)
	 * @param body          Aggregate 資料
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
	public void addEventSource(String eventStreamId, String body) {
		// 取得 Event
		BaseEvent event = ContextHolder.getEvent();

		EventSource eventSource = eventSourceRepository.findTopByUuidOrderByVersionDesc(eventStreamId);

		if (Objects.isNull(eventSource)) {
			// 若沒有 EventSourcing 紀錄，新增第一筆
			eventSource = EventSource.builder().uuid(eventStreamId).targetId(event.getTargetId())
					.className(event.getClass().getName()).body(body).userId("SYSTEM").occuredAt(new Date()).version(1L)
					.status(StatusCode.SUCCESS).build();
			eventSourceRepository.save(eventSource);
		} else {
			// 紀錄 EventSourcing (VERSION+1)
			EventSource entity = EventSource.builder().uuid(eventStreamId).targetId(event.getTargetId())
					.className(event.getClass().getName()).body(body).userId("SYSTEM").occuredAt(new Date())
					.status(StatusCode.SUCCESS).version(eventSource.getVersion() + 1).build();
			eventSourceRepository.save(entity);
		}

		log.debug("[Add Event Sourcing] event:{}, eventSource:{}", event, eventSource);
	}

	/**
	 * 建立 EventLog
	 * 
	 * @param topicQueue   Topic 通道
	 * @param eventLogUuid EventLog 的 UUID
	 * @param targetId     目標物 UUID
	 * @param event        事件
	 * @return EventLog
	 */
	public EventLog generateEventLog(String topicQueue, String eventLogUuid, String targetId, BaseEvent event) {
		// 建立 EventLog
		EventLog eventLog = EventLog.builder().uuid(eventLogUuid).topic(topicQueue).targetId(targetId)
				.className(event.getClass().getName()).body(JsonParseUtil.serialize(event)).userId("SYSTEM").build();
		return eventLogRepository.save(eventLog);
	}
}
