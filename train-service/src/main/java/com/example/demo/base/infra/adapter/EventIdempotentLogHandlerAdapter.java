package com.example.demo.base.infra.adapter;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.base.application.port.EventIdempotenceHandlerPort;
import com.example.demo.base.infra.persistence.EventIdempotentLogRepository;
import com.example.demo.base.shared.command.BaseIdempotentCommand;
import com.example.demo.base.shared.entity.EventIdempotentLog;
import com.example.demo.base.shared.event.BaseEvent;

/**
 * Event Idempotent Service 用於執行冪等機制的 Service，防止重複消費的副作用
 */
@Component
class EventIdempotentLogHandlerAdapter implements EventIdempotenceHandlerPort {

	private EventIdempotentLogRepository repository;

	public EventIdempotentLogHandlerAdapter(EventIdempotentLogRepository repository) {
		this.repository = repository;
	}

	/**
	 * 執行 Event 的冪等機制
	 * 
	 * @param event 事件
	 * @return boolean
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.DEFAULT, timeout = 3600, rollbackFor = Exception.class)
	public boolean handleIdempotency(BaseEvent event) {
		boolean result = false;
		List<EventIdempotentLog> logList = repository.findByEventTypeAndUniqueKey(event.getClass().getName(),
				event.getEventLogUuid());
		// 若查無資料
		if (logList.isEmpty()) {
			repository.insert(event.getClass().getName(), event.getEventLogUuid(), event.getTargetId());
			result = true;
		}
		return result;
	}

	/**
	 * 執行 非 Event 的冪等機制
	 * 
	 * @param command
	 * @return boolean
	 */
	public boolean handleIdempotency(BaseIdempotentCommand command) {
		boolean result = false;
		List<EventIdempotentLog> logList = repository.findByEventTypeAndUniqueKey(command.getClass().getName(),
				command.getEventLogUuid());
		// 若查無資料
		if (logList.isEmpty()) {
			repository.insert(command.getClass().getName(), command.getEventLogUuid(), command.getTargetId());
			result = true;
		}
		return result;
	}

}
