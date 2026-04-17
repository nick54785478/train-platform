package com.example.demo.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 日期資料轉換器
 */
@Slf4j
@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateTransformUtil {

	/**
	 * 將 LocalDate 轉為 字串
	 */
	public static String transformLocalDateToString(LocalDate localDate) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		return localDate.format(formatter);
	}

	/**
	 * 將 字串 轉為 LocalDate
	 */
	public static LocalDate transformStringToLocalDate(String localDate) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		return LocalDate.parse(localDate, formatter);
	}

	/**
	 * String 轉換 Date
	 */
	public static Date parse(String pattern, String date) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
		if (StringUtils.isBlank(date)) {
			return null;
		}

		return transformLocalDateTimeToDate(LocalDateTime.parse(date, formatter));
	}

	/**
	 * Date 轉換 String
	 */
	public static String format(String pattern, Date date) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
		if (Objects.isNull(date)) {
			return null;
		}
		LocalDateTime localDateTime = transformDateToLocalDateTime(date);
		return localDateTime.format(formatter);
	}

	/**
	 * 將 LocalDateTime 轉換為 Date
	 */
	public static Date transformLocalDateTimeToDate(LocalDateTime date) {
		if (Objects.isNull(date)) {
			return null;
		}
		return Date.from(date.atZone(ZoneId.systemDefault()).toInstant());
	}

	/**
	 * 將 Date 轉換為 LocalDate
	 */
	public static LocalDate transformDateToLocalDate(Date date) {
		if (Objects.isNull(date)) {
			return null;
		}
		Instant instant = date.toInstant();
		return instant.atZone(ZoneId.systemDefault()).toLocalDate();
	}

	/**
	 * 將 Date 轉換為 LocalDateTime
	 */
	public static LocalDateTime transformDateToLocalDateTime(Date date) {
		if (Objects.isNull(date)) {
			return null;
		}
		Instant instant = date.toInstant();
		return instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
	}

	/**
	 * 根據 period 取得 第一天 (月、季、年)
	 */
	public static Date getFirstDayAccordingPeriod(String period) {
		if (StringUtils.equals(period, "YTD")) {
			return getFirstDayThisYear();
		} else if (StringUtils.equals(period, "QTD")) {
			return getFirstDayThisQuarter();
		} else {
			return getFirstDayThisMonth();
		}
	}

	/**
	 * 取得該季第一天
	 */
	private static Date getFirstDayThisQuarter() {
		// 取得當前日期
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		// 確定當前月份所處的季度
		int quarter = (calendar.get(Calendar.MONTH) / 3) + 1;
		// 設置日期為該季度的第一天
		int firstMonthOfQuarter = (quarter - 1) * 3;
		calendar.set(Calendar.MONTH, firstMonthOfQuarter); // Calendar.MONTH 從0開始（0代表一月，1代表二月，以此類推）。
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		log.info("該季度的第一天日期是: " + calendar.getTime());
		return calendar.getTime();
	}

	/**
	 * 取得當月第一天
	 */
	private static Date getFirstDayThisMonth() {
		// 取得當前日期
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		// 設置日期為當月的第一天
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		log.info("當月第一天的日期是: " + calendar.getTime());
		// 取得當月的第一天日期
		return calendar.getTime();
	}

	/**
	 * 取得當年第一天
	 */
	private static Date getFirstDayThisYear() {
		// 取得當前日期
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());

		// 設置日期為當年的第一天
		calendar.set(Calendar.MONTH, Calendar.JANUARY);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		log.info("當年的第一天日期是: " + calendar.getTime());
		// 取得當年的第一天日期
		return calendar.getTime();
	}

}
