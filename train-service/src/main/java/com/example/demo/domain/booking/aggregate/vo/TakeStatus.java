package com.example.demo.domain.booking.aggregate.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum TakeStatus {
	
	UNTAKEN("UNTAKEN", "尚未搭乘"), TAKEN("TAKEN", "已搭乘"), CANCELLED("CANCELLED", "取消訂位");

	@Getter
	private final String name;

	@Getter
	private final String value;
}