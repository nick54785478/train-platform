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
public class BookingSagaConfiguration {

	@Value("${rabbitmq.exchange.name}")
	private String exchangeName;

	@Value("${rabbitmq.booking.saga.start}")
	private String sagaStart;

	@Value("${rabbitmq.booking.saga.process}")
	private String sagaProcess;

	@Value("${rabbitmq.booking.saga.completion}")
	private String sagaCompletion;

	@Value("${rabbitmq.booking.saga.compensation}")
	private String sagaCompensation;

	@Value("${rabbitmq.booking.saga.failure}")
	private String sagaFailure;

	@Value("${rabbitmq.booking.saga.cancel}")
	private String sagaCancel;

	/**
	 * SAGA Start Queue
	 * 
	 * @return sagaStartQueue
	 */
	@Bean
	public Queue sagaStartQueue() {
		return new Queue(sagaStart, true);
	}

	/**
	 * SAGA Process Queue
	 * 
	 * @return sagaProcessQueue
	 */
	@Bean
	public Queue sagaProcessQueue() {
		return new Queue(sagaProcess, true);
	}

	/**
	 * SAGA Completion Queue
	 * 
	 * @return sagaCompletionQueue
	 */
	@Bean
	public Queue sagaCompletionQueue() {
		return new Queue(sagaCompletion, true);
	}

	/**
	 * SAGA Compensation Queue
	 * 
	 * @return sagaCompensationQueue
	 */
	@Bean
	public Queue sagaCompensationQueue() {
		return new Queue(sagaCompensation, true);
	}

	/**
	 * SAGA Failure Queue
	 * 
	 * @return sagaFailureQueue
	 */
	@Bean
	public Queue sagaFailureQueue() {
		return new Queue(sagaFailure, true);
	}

	/**
	 * SAGA Cancel Queue
	 * 
	 * @return sagaCancelQueue
	 */
	@Bean
	public Queue sagaCancelQueue() {
		return new Queue(sagaCancel, true);
	}

//	/**
//     * 宣告 Routing Key (通常建議定義在 properties 或常數中)
//     */
//    private static final String ROUTING_KEY_START = "booking.saga.start";
//    private static final String ROUTING_KEY_PROCESS = "booking.saga.process";
//    private static final String ROUTING_KEY_COMPLETION = "booking.saga.completion";
//    private static final String ROUTING_KEY_COMPENSATION = "booking.saga.compensation";

	// 1. 綁定 Start Queue 到 Exchange
	@Bean
	public Binding startBinding(Queue sagaStartQueue, TopicExchange topicExchange) {
		return BindingBuilder.bind(sagaStartQueue).to(topicExchange).with(sagaStart);
	}

	// 2. 綁定 Process Queue 到 Exchange
	@Bean
	public Binding processBinding(Queue sagaProcessQueue, TopicExchange topicExchange) {
		return BindingBuilder.bind(sagaProcessQueue).to(topicExchange).with(sagaProcess);
	}

	// 3. 綁定 Completion Queue 到 Exchange
	@Bean
	public Binding completionBinding(Queue sagaCompletionQueue, TopicExchange topicExchange) {
		return BindingBuilder.bind(sagaCompletionQueue).to(topicExchange).with(sagaCompletion);
	}

	// 4. 綁定 Compensation Queue 到 Exchange
	@Bean
	public Binding compensationBinding(Queue sagaCompensationQueue, TopicExchange topicExchange) {
		return BindingBuilder.bind(sagaCompensationQueue).to(topicExchange).with(sagaCompensation);
	}

	// 5. 綁定 Failure Queue 到 Exchange
	@Bean
	public Binding failureBinding(Queue sagaFailureQueue, TopicExchange topicExchange) {
		return BindingBuilder.bind(sagaFailureQueue).to(topicExchange).with(sagaFailure);
	}

	// 6. 綁定 Cancel Queue 到 Exchange
	@Bean
	public Binding cancelBinding(Queue sagaCancelQueue, TopicExchange topicExchange) {
		return BindingBuilder.bind(sagaCancelQueue).to(topicExchange).with(sagaCancel);
	}
}
