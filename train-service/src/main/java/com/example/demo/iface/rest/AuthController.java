package com.example.demo.iface.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.application.service.AuthCommandService;
import com.example.demo.base.shared.command.GetJwtTokenCommand;
import com.example.demo.iface.dto.res.GetJwtTokenResource;
import com.example.demo.iface.dto.res.JwtTokenGettenResource;
import com.example.demo.util.BaseDataTransformer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@AllArgsConstructor
@Tag(name = "Authority Login API", description = "進行會員登入")
@RequestMapping("/api/v1/login")
public class AuthController {

	private AuthCommandService authCommandService;

	/**
	 * 進行會員登入
	 * 
	 * @param resource
	 * @return JWT Token
	 */
	@PostMapping("")
	@Operation(summary = "API - 進行會員登入", description = "進行會員登入。")
	public ResponseEntity<JwtTokenGettenResource> login(
			@Parameter(description = "登入資訊(帳號、密碼)", required = true) @RequestBody GetJwtTokenResource resource) {
		GetJwtTokenCommand command = BaseDataTransformer.transformData(resource, GetJwtTokenCommand.class);
		return new ResponseEntity<>(
				BaseDataTransformer.transformData(authCommandService.getToken(command), JwtTokenGettenResource.class),
				HttpStatus.OK);
	}
}
