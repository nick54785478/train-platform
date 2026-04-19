package com.example.demo.application.port;

/**
 * Port 定義：事件 -> Topic Resolver。
 *
 * <p>
 * 提供應用層查詢事件對應 MQ Topic 的接口。
 */
public interface EventTopicResolverPort {

	/**
	 * 根據事件實例解析對應的 Topic。
	 *
	 * @param event 事件實例
	 * @return 事件對應的 MQ Topic
	 * @throws IllegalArgumentException 若事件類型未對應 Topic
	 */
	String resolve(Object event);
}