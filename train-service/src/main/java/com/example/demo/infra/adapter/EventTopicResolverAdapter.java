package com.example.demo.infra.adapter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.example.demo.application.port.EventTopicResolverPort;
import com.example.demo.base.infra.annotation.EventTopic;

import lombok.extern.slf4j.Slf4j;

/**
 * Adapter 實作 {@link EventTopicResolverPort}，負責解析事件類對應的 MQ Topic。
 *
 * <p>
 * 主要功能：
 * <ul>
 * <li>掃描 com.example.demo.domain 包下的所有 {@link EventTopic} 註解類</li>
 * <li>建立事件類別 -> Topic 映射表</li>
 * <li>提供事件對應 Topic 的查詢方法</li>
 * </ul>
 *
 * <p>
 * Hexagonal 架構中，此類屬於 Adapter 層，依賴 Spring {@link Environment} 獲取 Topic 配置。
 */
@Slf4j
@Component
class EventTopicResolverAdapter implements EventTopicResolverPort {

	private final Map<Class<?>, String> eventTopicMap = new HashMap<>();

	/**
	 * 建構子：掃描事件類並建立 Topic 映射。
	 *
	 * @param env Spring Environment，用於解析 {@link EventTopic} 的占位符
	 * @throws Exception 若反射掃描失敗則拋出
	 */
	public EventTopicResolverAdapter(Environment env) throws Exception {
		Reflections reflections = new Reflections("com.example.demo.domain"); // Domain Event 都落在 Domain 層
		Set<Class<?>> eventClasses = reflections.getTypesAnnotatedWith(EventTopic.class);

		for (Class<?> clazz : eventClasses) {
			EventTopic annotation = clazz.getAnnotation(EventTopic.class);
			String topic = env.resolvePlaceholders(annotation.value());
			eventTopicMap.put(clazz, topic);
			log.info("建立成功: topic:{}, event:{}", topic, clazz);
		}
	}

	/**
	 * 解析事件對應的 Topic。
	 *
	 * @param event 事件實例
	 * @return 該事件對應的 Topic
	 * @throws IllegalArgumentException 若事件類型未對應 Topic
	 */
	@Override
	public String resolve(Object event) {
		String topic = eventTopicMap.get(event.getClass());
		if (topic == null) {
			throw new IllegalArgumentException("No topic mapping for event: " + event.getClass());
		}
		return topic;
	}
}