package com.example.demo.infra.adapter;

import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.demo.base.application.port.EventPublisherPort;
import com.example.demo.base.shared.command.PublishEventCommand;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
class RabbitmqService implements EventPublisherPort {

	private final RabbitTemplate rabbitTemplate;

	@Value("${rabbitmq.exchange.name}")
	private String exchangeName;

	/**
	 * 發布事件到 Rabbit MQ
	 * 
	 * @param exchange   交換機
	 * @param topicQueue Topic 通道
	 * @param event      事件
	 */
	@Override
	public void publish(PublishEventCommand<?> command) {
		log.debug("Exchange:{}, Topic:{}, Event Body:{}", exchangeName, command.getTopic(), command.getEvent());
		rabbitTemplate.convertAndSend(exchangeName, command.getTopic(), command.getEvent());
	}

	/**
	 * 發布事件到 Rabbit MQ
	 * 
	 * @param exchange   交換機
	 * @param topicQueue Topic 通道
	 * @param event      事件
	 */
	@Override
	public void publish(List<PublishEventCommand<?>> commands) {
		commands.stream().forEach(this::publish);

	}
}
