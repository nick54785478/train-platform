package com.example.demo.base.shared.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * JWToken 應用常數
 */
@AllArgsConstructor
public enum JwtConstants {

	/**
	 * HTTP 請求 Header 中用於攜帶 JWT 的名稱。
	 */
	JWT_HEADER("Authorization"),

	/**
	 * JWT 的前綴。
	 */
	JWT_PREFIX("Bearer "),

	/**
	 * JWT 中用於標識主體的索引鍵。
	 */
	JWT_CLAIM_KEY_SUB("sub"),

	/**
	 * JWT 在程式碼中統一引用 JWT 中角色資訊的欄位名稱
	 */
	JWT_CLAIMS_KEY_ROLE("roles"),

	/**
	 * JWT 在程式碼中統一引用 JWT 中使用者帳號
	 */
	JWT_CLAIMS_KEY_USER("username"),

	/**
	 * JWT 在程式碼中統一引用 JWT 中使用者信箱
	 */
	JWT_CLAIMS_KEY_EMAIL("email");

	@Getter
	private String value;

}