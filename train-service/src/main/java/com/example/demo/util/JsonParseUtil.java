package com.example.demo.util;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Json 轉換器
 */
@Slf4j
@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonParseUtil {

	protected static final ObjectMapper mapper = new ObjectMapper();

	static {
		mapper.registerModule(new JavaTimeModule()); // 支援 LocalDate / LocalDateTime
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // 不用 timestamp
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
	}

	/**
	 * 序列化物件 為 JSON
	 * 
	 * @param target 目標物件
	 * @return 序列化 JSON 字串
	 */
	public static String serialize(Object target) {
		try {
			mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
			return mapper.writeValueAsString(target);
		} catch (JsonProcessingException e) {
			log.error("Occurred JsonMapping Exception", e);
			return "";
		}
	}

	/**
	 * 反序列化 JSON 回 物件
	 * 
	 * @param target  目標 序列化 JSON 字串
	 * @param 欲轉換物件類型
	 * @return 反序列化物件
	 */
	public static <T> T unserialize(String target, Class<T> clazz) {
		try {
			mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
			return mapper.readValue(target, clazz);
		} catch (JsonMappingException e) {
			log.error("Occurred JsonMapping Exception", e);
			return null;
		} catch (JsonProcessingException e) {
			log.error("Occurred JsonProcessing Exception", e);
			return null;
		}
	}

}
