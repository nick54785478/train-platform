package com.example.demo.base.infra.adapter;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import com.example.demo.base.application.port.DataTransformerPort;
import com.example.demo.base.shared.enums.YesNo;

import lombok.extern.slf4j.Slf4j;

/**
 * Adapter 實作 {@link DataTransformerPort}，負責資料轉換。
 *
 * <p>
 * 功能：
 * <ul>
 * <li>使用 ModelMapper 封裝各種資料型別轉換</li>
 * <li>支援 LocalDate、Date、Long、BigDecimal、YesNo 等轉換</li>
 * <li>提供單物件及 List 轉換方法</li>
 * </ul>
 */
@Slf4j
@Component
public class DataTransformerAdapter implements DataTransformerPort {

	private final ModelMapper modelMapper;

	public DataTransformerAdapter() {
		this.modelMapper = new ModelMapper();
		this.initConverters(); // 初始化資料型別轉換器
	}

	@Override
	public <T> T transform(Object source, Class<T> clazz) {
		return modelMapper.map(source, clazz);
	}

	@Override
	public <S, T> List<T> transformList(List<S> sourceList, Class<T> clazz) {
		return sourceList.stream().map(e -> transform(e, clazz)).collect(Collectors.toList());
	}
	
	/**
	 * 初始化資料型別轉換器
	 */
	private void initConverters() {
		var simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		// LocalDate -> String
		modelMapper.addConverter((Converter<LocalDate, String>) context -> context.getSource() == null ? null
				: context.getSource().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

		// String -> LocalDate
		modelMapper.addConverter((Converter<String, LocalDate>) context -> {
			if (context.getSource() == null)
				return null;
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			return LocalDate.parse(context.getSource(), formatter);
		});

		// Date -> String
		modelMapper.addConverter((Converter<Date, String>) context -> context.getSource() == null ? null
				: simpleDateFormat.format(context.getSource()));

		// String -> Date
		modelMapper.addConverter((Converter<String, Date>) context -> {
			if (context.getSource() == null)
				return null;
			try {
				return simpleDateFormat.parse(context.getSource());
			} catch (ParseException e) {
				log.error("日期轉換失敗", e);
				return null;
			}
		});

		// Long <-> String
		modelMapper.addConverter((Converter<Long, String>) context -> context.getSource() == null ? null
				: context.getSource().toString());
		modelMapper.addConverter((Converter<String, Long>) context -> {
			if (context.getSource() == null)
				return null;
			try {
				return Long.parseLong(context.getSource());
			} catch (NumberFormatException e) {
				log.error("String 轉 Long 失敗", e);
				return null;
			}
		});

		// String -> BigDecimal
		modelMapper.addConverter((Converter<String, BigDecimal>) context -> {
			if (context.getSource() == null)
				return null;
			try {
				return new BigDecimal(context.getSource());
			} catch (NumberFormatException e) {
				log.error("String 轉 BigDecimal 失敗", e);
				return null;
			}
		});

		// BigDecimal -> String
		modelMapper.addConverter((Converter<BigDecimal, String>) context -> context.getSource() == null ? null
				: context.getSource().toString());

		// YesNo <-> String
		modelMapper.addConverter((Converter<YesNo, String>) context -> context.getSource() == null ? null
				: context.getSource().getValue());
		modelMapper.addConverter((Converter<String, YesNo>) context -> context.getSource() == null ? null
				: YesNo.valueOf(context.getSource()));
	}
}