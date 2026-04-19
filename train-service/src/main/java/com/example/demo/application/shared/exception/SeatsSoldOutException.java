package com.example.demo.application.shared.exception;

import java.time.LocalDate;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)

public class SeatsSoldOutException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

	public SeatsSoldOutException(String trainUuid, LocalDate takeDate) {
		super(String.format("車次 %s 在 %s 已無剩餘座位可供預訂", trainUuid, takeDate));
	}
}