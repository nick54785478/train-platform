package com.example.demo.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

@EnableRabbit
@Configuration(proxyBeanMethods = false)
public class RabbitmqConfiguration {

	@Value("${rabbitmq.exchange.name}")
	private String exchangeName;

	@Value("${rabbitmq.topic.test}")
	private String testQueueName;

	@Value("${rabbitmq.topic.account.transaction}")
	private String accountTxQueueName;

	/**
	 * 將自定義的消息類序列化成 json 格式，再轉成 byte構造 Message，在接收消息時，會將接收到的 Message 再反序列化成自定義的類。
	 * 
	 * @param objectMapper
	 * @return MessageConverter
	 */
	@Bean
	public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
		return new Jackson2JsonMessageConverter(objectMapper);
	}

	/**
	 * 設置 Topic Exchange (主題交換機)
	 * 
	 * @return TopicExchange
	 */
	@Bean
	public TopicExchange topicExchange() {
		/*
		 * topicExchange的參數說明: 1. 交換器名稱 2. 是否持久化 true：持久化，交換器一直保留 false：不持久化，用完就刪除 3.
		 * 是否自動刪除 false：不自動刪除 true：自動刪除
		 */
		return new TopicExchange(exchangeName, true, false);
	}

	/**
	 * 新增 Rabbit Test Queue
	 * 
	 * @return testQueue
	 */
	@Bean
	public Queue testQueue() {
		return new Queue(testQueueName, true);
	}

	/**
	 * 新增 Rabbit Account Transaction Queue
	 * 
	 * @return accountTxQueueQueue
	 */
	@Bean
	public Queue accountTxQueue() {
		return new Queue(accountTxQueueName, true);
	}

	/**
	 * 將佇列 (Test Queue) 綁定到主題交換器 (TopicExchange)，並指定路由鍵模式。
	 */
	@Bean
	public Binding testTopicQueueBinding(Queue testQueue, TopicExchange exchange) {
		// 結合設定檔設定的匹配規則
		// * ：有且僅有一個
		// #：匹配0個或者多個
		return BindingBuilder.bind(testQueue).to(exchange).with(testQueueName); // 註. bind 需與上方 Queue 名稱一致
//		return BindingBuilder.bind(testQueue()).to(topicExchange()).with("*.test.#");
	}

	/**
	 * 將佇列 (Test Queue) 綁定到主題交換器 (TopicExchange)，並指定路由鍵模式。
	 */
	@Bean
	public Binding accountTxTopicQueueBinding(Queue accountTxQueue, TopicExchange exchange) {
		// 結合設定檔設定的匹配規則
		// * ：有且僅有一個
		// #：匹配0個或者多個
		return BindingBuilder.bind(accountTxQueue).to(exchange).with(accountTxQueueName); // 註. bind 需與上方 Queue 名稱一致
//		return BindingBuilder.bind(testQueue()).to(topicExchange()).with("*.test.#");
	}

}
