package com.example.demo.infra.adapter;

import org.springframework.stereotype.Component;

import com.example.demo.base.application.port.AuthServiceClientPort;
import com.example.demo.base.shared.dto.JwtTokenValidatedAndParsedResource;
import com.example.demo.domain.account.command.RegisterUserCommand;
import com.example.demo.domain.share.UserLoginCommand;
import com.example.demo.iface.dto.res.JwtTokenGettenResource;
import com.example.demo.iface.dto.res.UserInfoGottenResource;
import com.example.demo.iface.dto.res.UserRegisteredResource;
import com.example.demo.infra.client.AuthFeignClient;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
class AuthServiceClientAdatper implements AuthServiceClientPort {

	private AuthFeignClient authFeignClient;

	@Override
	public UserRegisteredResource register(RegisterUserCommand command) {
		return authFeignClient.register(command);
	}

	@Override
	public JwtTokenGettenResource login(UserLoginCommand command) {
		return authFeignClient.login(command);
	}

	@Override
	public UserInfoGottenResource getUserByEmail(String email) {
		return authFeignClient.getUserByEmail(email);
	}

	@Override
	public JwtTokenValidatedAndParsedResource validateJwtToken(String jwtToken) {
		return authFeignClient.validate(jwtToken);
	}

}
