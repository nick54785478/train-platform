package com.example.demo.base.application.port;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

public interface JsonTransformerPort {

	/**
	 * 序列化物件 為 JSON
	 * 
	 * @param target 目標物件
	 * @return 序列化 JSON 字串
	 * @throws JsonProcessingException
	 */
	String stringify(Object target) throws JsonProcessingException;

	/**
	 * 反序列化 JSON 回 物件
	 * 
	 * @param target  目標 序列化 JSON 字串
	 * @param 欲轉換物件類型
	 * @return 反序列化物件
	 * @throws JsonProcessingException
	 * @throws JsonMappingException
	 */
	<T> T parse(String target, Class<T> clazz) throws JsonProcessingException;
}
