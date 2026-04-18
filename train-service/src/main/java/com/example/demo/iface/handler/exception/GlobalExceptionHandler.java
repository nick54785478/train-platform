package com.example.demo.iface.handler.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.demo.base.shared.exception.exception.ResourceNotFoundException;
import com.example.demo.base.shared.exception.exception.ValidationException;
import com.example.demo.base.shared.exception.response.BaseExceptionResponse;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;

/**
 * 全域例外處理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * 處理驗證例外
	 * 
	 */
	@ExceptionHandler(ValidationException.class)
	public ResponseEntity<BaseExceptionResponse> handleValidationException(ValidationException e) {
		return ResponseEntity.status(HttpStatus.OK).body(new BaseExceptionResponse(e.getCode(), e.getMessage()));
	}

	/**
	 * 處理 jwt token 過期例外
	 */
	@ExceptionHandler(ExpiredJwtException.class)
	public ResponseEntity<BaseExceptionResponse> handleExpiredJwtExceptionHandle(final ExpiredJwtException e) {
		log.error("Token 已過期，請重新登入!", e);
		return ResponseEntity.status(HttpStatus.OK).body(new BaseExceptionResponse("401", "Token 已過期，請重新登入!"));
	}

	/**
	 * 處理 jwt token 的簽名例外
	 */
	@ExceptionHandler(SignatureException.class)
	public ResponseEntity<BaseExceptionResponse> handleSignatureException(final SignatureException e) {
		log.error("簽名有誤", e);
		return ResponseEntity.status(HttpStatus.OK).body(new BaseExceptionResponse("403", "Token 驗證不符，拒絕存取!"));
	}

	/**
	 * 處理查無資料相關的例外
	 */
	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<BaseExceptionResponse> handleResourceNotFoundException(final ResourceNotFoundException e) {
		return ResponseEntity.status(HttpStatus.OK).body(new BaseExceptionResponse(e.getCode(), e.getMessage()));
	}

	/**
	 * 處理 IllegalArgumentException 方法
	 * <p>
	 * IllegalArgumentException 表示方法接收到了不合法或不適當的參數。這通常意謂著傳入的參數值超出了預期範圍、格式錯誤或邏輯上不合理。
	 * </p>
	 */
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<BaseExceptionResponse> handleIllegalArgumentException(final IllegalArgumentException e) {
		return ResponseEntity.status(HttpStatus.OK).body(new BaseExceptionResponse("ILLEGAL_ARGUMENT", e.getMessage()));
	}

	/**
	 * 處理 IllegalStateException 方法
	 * <p>
	 * IllegalStateException 表示在不恰當的時間呼叫了方法，或應用程式當前狀態不符合方法的要求，通常代表「狀態錯誤」。
	 * </p>
	 */
	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<BaseExceptionResponse> handleIllegalStateException(final IllegalStateException e) {
		return ResponseEntity.status(HttpStatus.OK).body(new BaseExceptionResponse("ILLEGAL_STATE", e.getMessage()));
	}

	/**
	 * 處理 Exception 方法，這裡通常為非預期的例外
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<BaseExceptionResponse> handleException(final Exception e) {
		log.error("發生非預期錯誤 ", e);
		return ResponseEntity.status(HttpStatus.OK).body(new BaseExceptionResponse("500", "發生非預期錯誤"));
	}

}
