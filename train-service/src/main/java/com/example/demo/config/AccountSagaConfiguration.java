package com.example.demo.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableRabbit
@Configuration(proxyBeanMethods = false)
public class AccountSagaConfiguration {

	@Value("${rabbitmq.exchange.name}")
	private String exchangeName;

	@Value("${rabbitmq.account.saga.start}")
	private String sagaStart;

	@Value("${rabbitmq.account.saga.completion}")
	private String sagaCompletion;

	@Value("${rabbitmq.account.saga.compensation}")
	private String sagaCompensation;

	@Value("${rabbitmq.account.saga.failure}")
	private String sagaFailure;

	/**
	 * SAGA Start Queue
	 * 
	 * @return sagaStartQueue
	 */
	@Bean
	public Queue accountSagaStartQueue() {
		return new Queue(sagaStart, true);
	}

	/**
	 * SAGA Completion Queue
	 * 
	 * @return sagaCompletionQueue
	 */
	@Bean
	public Queue accountSagaCompletionQueue() {
		return new Queue(sagaCompletion, true);
	}

	/**
	 * SAGA Compensation Queue
	 * 
	 * @return sagaCompensationQueue
	 */
	@Bean
	public Queue accountSagaCompensationQueue() {
		return new Queue(sagaCompensation, true);
	}

	/**
	 * SAGA Failure Queue
	 * 
	 * @return sagaFailureQueue
	 */
	@Bean
	public Queue accountSagaFailureQueue() {
		return new Queue(sagaFailure, true);
	}

	// 1. 綁定 Start Queue 到 Exchange
	@Bean
	public Binding accountSagaStartBinding(Queue accountSagaStartQueue, TopicExchange topicExchange) {
		return BindingBuilder.bind(accountSagaStartQueue).to(topicExchange).with(sagaStart);
	}

	// 2. 綁定 Completion Queue 到 Exchange
	@Bean
	public Binding accountSagaCompletionBinding(Queue accountSagaCompletionQueue, TopicExchange topicExchange) {
		return BindingBuilder.bind(accountSagaCompletionQueue).to(topicExchange).with(sagaCompletion);
	}

	// 3. 綁定 Compensation Queue 到 Exchange
	@Bean
	public Binding accountSagaCompensationBinding(Queue accountSagaCompensationQueue, TopicExchange topicExchange) {
		return BindingBuilder.bind(accountSagaCompensationQueue).to(topicExchange).with(sagaCompensation);
	}

	// 4. 綁定 Failure Queue 到 Exchange
	@Bean
	public Binding accountSagaFailureBinding(Queue accountSagaFailureQueue, TopicExchange topicExchange) {
		return BindingBuilder.bind(accountSagaFailureQueue).to(topicExchange).with(sagaFailure);
	}
}
