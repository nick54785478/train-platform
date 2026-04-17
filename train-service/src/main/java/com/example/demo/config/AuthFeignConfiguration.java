package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.demo.base.infra.context.ContextHolder;
import com.example.demo.base.shared.enums.JwtConstants;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class AuthFeignConfiguration {

	@Value("${auth.endpoint.service.jwt.token}")
	private String token;

	/**
	 * 定義一個 Feign 的請求攔截器，用於在每次發送請求時增加 Header 資料。
	 *
	 * @return 請求攔截器
	 */
	@Bean
	public RequestInterceptor requestTokenInterceptor() {

		return new RequestInterceptor() {
			@Override
			public void apply(RequestTemplate requestTemplate) {
				// 在此處新增 JWToken Request Header
				String jwtoken = ContextHolder.getJwtoken();
				log.info("[requestTokenInterceptor] jwtoken :{}", jwtoken);
				requestTemplate.header(JwtConstants.JWT_HEADER.getValue(),
						JwtConstants.JWT_PREFIX.getValue() + jwtoken);
			}
		};
	}
}
