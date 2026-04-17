package com.example.demo.base.infra.context;

import java.util.List;

import com.example.demo.base.shared.event.BaseEvent;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 上下文工具類
 */
@Slf4j
@NoArgsConstructor
public class ContextHolder {

	/**
	 * JWT Claims 的 Key，用於指定 JWT 的 Subject
	 */
	public static final String CLAIM_KEY_SUB = "sub";

	/**
	 * JWT Claims 的 Key，用於指定 JWT 的 Acc
	 */
	public static final String CLAIM_KEY_ACC = "acc";

	/**
	 * 儲存目前使用者傳入的使用者帳號
	 */
	public static final ThreadLocal<String> USERNAME = new ThreadLocal<>();

	/**
	 * 儲存目前使用者傳入的信箱
	 */
	public static final ThreadLocal<String> EMAIL = new ThreadLocal<>();

	/**
	 * 儲存目前使用者傳入的角色清單
	 */
	public static final ThreadLocal<List<String>> ROLELIST = new ThreadLocal<>();

	/**
	 * 儲存目前使用者傳入的 JWT Token
	 */
	private static final ThreadLocal<String> JWT_TOKEN = new ThreadLocal<>();

	/**
	 * 儲存 Event
	 */
	private static final ThreadLocal<BaseEvent> EVENT = new ThreadLocal<>();

	/**
	 * 把 JWT Token 設定到 ThreadLocal 內
	 * 
	 * @param claims
	 */
	public static void setJwtToken(String token) {
		JWT_TOKEN.set(token);
	}

	/**
	 * 把 使用者帳號 設定到 ThreadLocal 內
	 * 
	 * @param username
	 */
	public static void setUsername(String username) {
		USERNAME.set(username);
	}

	/**
	 * 把 信箱 設定到 ThreadLocal 內
	 * 
	 * @param email
	 */
	public static void setEmail(String email) {
		EMAIL.set(email);
	}

	/**
	 * 把 角色清單 設定到 ThreadLocal 內
	 * 
	 * @param roles
	 */
	public static void setRoleList(List<String> roles) {
		ROLELIST.set(roles);
	}

	/**
	 * 把 BaseEvent 設定到 ThreadLocal 內
	 * 
	 * @param event
	 */
	public static void setBaseEvent(BaseEvent event) {
		EVENT.set(event);
	}

	/**
	 * 取得目前登入者的用戶帳號
	 * 
	 * @return 目前登入者的用戶帳號
	 */
	public static String getUsername() {
		return USERNAME.get();
	}

	/**
	 * 取得目前登入者的用戶信箱
	 * 
	 * @return 目前登入者的用戶信箱
	 */
	public static String getUserEmail() {
		return EMAIL.get();
	}

	/**
	 * 取得目前登入者的角色
	 * 
	 * @return 目前登入者的用戶帳號
	 */
	public static List<String> getRoleList() {
		return ROLELIST.get();
	}

	/**
	 * 取得目前登入者的 JwToken
	 * 
	 * @return token
	 */
	public static String getJwtoken() {
		return JWT_TOKEN.get() != null ? JWT_TOKEN.get() : null;
	}

	/**
	 * 獲取事件
	 * 
	 * @return Event 資訊
	 */
	public static BaseEvent getEvent() {
		return EVENT.get() != null ? EVENT.get() : null;
	}

	/**
	 * 清理上下文
	 */
	public static void clearContext() {
		EVENT.remove();
		EMAIL.remove();
		USERNAME.remove();
		ROLELIST.remove();
		JWT_TOKEN.remove();
	}

}