package com.example.demo.base.application.port;

import com.example.demo.base.shared.command.GetJwtTokenCommand;
import com.example.demo.base.shared.dto.JwtTokenValidatedAndParsedResource;
import com.example.demo.domain.account.command.RegisterUserCommand;
import com.example.demo.iface.dto.res.JwtTokenGettenResource;
import com.example.demo.iface.dto.res.UserInfoGottenResource;
import com.example.demo.iface.dto.res.UserRegisteredResource;

public interface AuthServiceClientPort {

	/**
	 * 註冊使用者資料
	 *
	 * @param resource
	 * @return 更新成功訊息
	 */
	public UserRegisteredResource register(RegisterUserCommand command);

	/**
	 * 登入功能
	 * 
	 * @param resource
	 * @return JwToken
	 */
	public JwtTokenGettenResource login(GetJwtTokenCommand command);

	/**
	 * 透過 email 取得 User 資料
	 * 
	 * @param request
	 * @return 使用者資料
	 */
	public UserInfoGottenResource getUserByEmail(String email);

	/**
	 * 解析 JWT Token
	 * 
	 * @param jwtTokwn JWT Token
	 * @return 解析後的資料
	 */
	public JwtTokenValidatedAndParsedResource validateJwtToken(String jwtToken);

}
