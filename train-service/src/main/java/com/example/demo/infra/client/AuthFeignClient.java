package com.example.demo.infra.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.base.shared.dto.JwtTokenValidatedAndParsedResource;
import com.example.demo.config.AuthFeignConfiguration;
import com.example.demo.domain.account.command.RegisterUserCommand;
import com.example.demo.domain.share.UserLoginCommand;
import com.example.demo.iface.dto.res.JwtTokenGettenResource;
import com.example.demo.iface.dto.res.UserInfoGottenResource;
import com.example.demo.iface.dto.res.UserRegisteredResource;

@FeignClient(value = "AuthFeignClient", url = "${auth.endpoint.service}", configuration = AuthFeignConfiguration.class)
public interface AuthFeignClient {

	/**
	 * 註冊使用者資料
	 *
	 * @param resource
	 * @return 更新成功訊息
	 */
	@PostMapping(value = "/api/v1/users/register")
	public UserRegisteredResource register(@RequestBody RegisterUserCommand command);

	/**
	 * 登入功能
	 * 
	 * @param resource
	 * @return JwToken
	 */
	@PostMapping(value = "/api/v1/login")
	public JwtTokenGettenResource login(@RequestBody UserLoginCommand command);

	/**
	 * 透過 email 取得 User 資料
	 * 
	 * @param request
	 * @return 使用者資料
	 */
	@GetMapping(value = "/api/v1/users/queryByEmail")
	public UserInfoGottenResource getUserByEmail(@RequestParam(required = false) String email);

	/**
	 * 驗證 JwToken
	 */
	@PostMapping(value = "/api/v1/auth/validate")
	public JwtTokenValidatedAndParsedResource validate(String jwtToken);
}
