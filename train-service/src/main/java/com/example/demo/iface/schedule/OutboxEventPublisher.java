package com.example.demo.iface.schedule;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.base.application.port.EventPublisherPort;
import com.example.demo.base.infra.persistence.EventLogRepository;
import com.example.demo.base.shared.command.PublishEventCommand;
import com.example.demo.base.shared.entity.EventLog;
import com.example.demo.base.shared.enums.EventLogSendQueueStatus;
import com.example.demo.util.JsonParseUtil;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor
public class OutboxEventPublisher {

	private final EventLogRepository eventLogRepository;
	private final EventPublisherPort rabbitmqService;

	/**
	 * 每 1 秒掃一次 Outbox
	 */
	@Scheduled(fixedDelay = 1000)
	@Transactional
	public void publishOutboxEvents() {

		List<EventLog> events = eventLogRepository.findTop100ByStatus(EventLogSendQueueStatus.INITIAL);

		for (EventLog event : events) {
			try {

				Class<?> clazz = Class.forName(event.getClassName());

				Object realEvent = JsonParseUtil.unserialize(event.getBody(), clazz);

				rabbitmqService.publish(PublishEventCommand.builder().topic(event.getTopic()).event(realEvent).build());

				event.publish();
				eventLogRepository.save(event);

			} catch (Exception e) {
				log.error("Outbox publish failed: {}", event.getUuid(), e);
			}
		}
	}
}