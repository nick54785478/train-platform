package com.example.demo.util;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.DateUtil;
import org.springframework.util.ReflectionUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 將 Map<屬性, 值> 資料 轉換為 Entity
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ParameterMappingUtil {

	/**
	 * 將 Map 的資料設置到實體中
	 * 
	 * @param entity
	 * @param fieldMap Field Map
	 * @return entity
	 */
	public static <T> T setFieldsFromMap(T entity, Map<String, String> fieldMap) {
		// 取得實體的 Class 類型
		Class<?> entityClass = entity.getClass();

		for (Map.Entry<String, String> entry : fieldMap.entrySet()) {
			String fieldName = entry.getKey();
			String fieldValue = entry.getValue();
			try {
				// 使用反射取得實體的對應屬性
				Field field = entityClass.getDeclaredField(fieldName);
				ReflectionUtils.makeAccessible(field);// 設置可訪問性: 如果函數是私有的，需要設權限

				// 根據屬性類型進行轉換
				Object convertedValue = convertToFieldType(field, fieldValue);
				// 設置轉換後的值
				field.set(entity, convertedValue);

			} catch (NoSuchFieldException e) {
				log.error("未找到屬性", e);
			} catch (IllegalAccessException e) {
				log.error("設置值發生錯誤", e);
			}
		}

		return entity;
	}

	/**
	 * 根據屬性類型轉換 String 值
	 * 
	 * @param field 參數
	 * @param value 值
	 */
	@SuppressWarnings("unchecked")
	private static Object convertToFieldType(Field field, String value) {
		Class<?> fieldType = field.getType();

		try {
			if (fieldType.equals(Long.class) || fieldType.equals(long.class)) {
				return Long.parseLong(value.replace(".0", ""));
			} else if (fieldType.equals(Integer.class) || fieldType.equals(int.class)) {
				return Integer.parseInt(value.replace(".0", ""));
			} else if (fieldType.equals(Double.class) || fieldType.equals(double.class)) {
				return Double.parseDouble(value);
			} else if (fieldType.equals(Boolean.class) || fieldType.equals(boolean.class)) {
				return Boolean.parseBoolean(value);
			} else if (fieldType.isEnum()) {
				@SuppressWarnings({ "rawtypes" })
				Class<Enum> enumType = (Class<Enum>) fieldType;
				return Enum.valueOf(enumType, value);
			} else if (fieldType.equals(BigDecimal.class)) {
				return new BigDecimal(value);
			}

			// 處理 Date 及 LocalDate / LocalTime / LocalDateTime
			else if (fieldType.equals(Date.class) || fieldType.equals(LocalDate.class)
					|| fieldType.equals(LocalTime.class) || fieldType.equals(LocalDateTime.class)) {

				// Excel 可能存儲為數字
				if (value.matches("^\\d+(\\.\\d+)?$")) {
					double numericValue = Double.parseDouble(value);
					Date javaDate = DateUtil.getJavaDate(numericValue); // 轉換為 java.util.Date
					if (fieldType.equals(Date.class)) {
						return javaDate;
					} else if (fieldType.equals(LocalDate.class)) {
						return javaDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
					} else if (fieldType.equals(LocalTime.class)) {
						return javaDate.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
					} else if (fieldType.equals(LocalDateTime.class)) {
						return javaDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
					}
				}

				// **若非數字，則用字串格式解析**
				if (fieldType.equals(LocalDate.class)) {
					return LocalDate.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
				} else if (fieldType.equals(LocalTime.class)) {
					return LocalTime.parse(value, DateTimeFormatter.ofPattern("HH:mm:ss"));
				} else if (fieldType.equals(LocalDateTime.class)) {
					return LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
				}
			}
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid format for field: " + field.getName() + " with value: " + value,
					e);
		}

		return value;
	}

	/**
	 * 獲取 Class 的所有字段
	 * 
	 * @param target
	 */
	public static <T> List<String> getFields(T target) {

		Field[] fields = target.getClass().getDeclaredFields();

		List<String> fieldNames = Arrays.stream(fields).map(Field::getName) // 獲取每個字段的名稱
				.collect(Collectors.toList());
		return fieldNames;
	}

}