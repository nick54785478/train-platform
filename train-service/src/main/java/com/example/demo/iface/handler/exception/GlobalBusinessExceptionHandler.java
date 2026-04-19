package com.example.demo.iface.handler.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.demo.application.shared.exception.SeatsSoldOutException;
import com.example.demo.base.shared.exception.response.BaseExceptionResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * 全域業務例外處理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalBusinessExceptionHandler {

	/**
	 * 座位售罄例外
	 */
	@ExceptionHandler(SeatsSoldOutException.class)
	public ResponseEntity<BaseExceptionResponse> handleSoldOut(SeatsSoldOutException e) {
		return ResponseEntity.status(HttpStatus.OK)
				.body(new BaseExceptionResponse("BUSINESS_EXCEPTION", e.getMessage()));
	}
}
