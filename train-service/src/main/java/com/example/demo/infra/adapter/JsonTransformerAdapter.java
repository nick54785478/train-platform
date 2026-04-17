package com.example.demo.infra.adapter;

import org.springframework.stereotype.Component;

import com.example.demo.base.application.port.JsonTransformerPort;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
class JsonTransformerAdapter implements JsonTransformerPort {

	protected static final ObjectMapper mapper = new ObjectMapper();

	static {
		mapper.registerModule(new JavaTimeModule()); // 支援 LocalDate / LocalDateTime
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // 不用 timestamp
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
	}

	@Override
	public String stringify(Object target) throws JsonProcessingException {
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		return mapper.writeValueAsString(target);
	}

	@Override
	public <T> T parse(String target, Class<T> clazz) throws JsonMappingException, JsonProcessingException {
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		return mapper.readValue(target, clazz);
	}

}
