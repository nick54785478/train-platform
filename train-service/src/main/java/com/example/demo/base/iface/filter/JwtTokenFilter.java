package com.example.demo.base.iface.filter;

import java.io.IOException;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.demo.base.application.port.AuthServiceClientPort;
import com.example.demo.base.infra.context.ContextHolder;
import com.example.demo.base.shared.dto.JwtTokenValidatedAndParsedResource;
import com.example.demo.base.shared.enums.JwtConstants;
import com.example.demo.base.shared.exception.exception.ValidationException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 用於攔截所有的 HTTP Request，從 Header 中提取授權資訊，將其轉換為 JWT（JSON Web Token）後， 存儲到
 * ContextHolder 內以供後續使用。
 */
@Slf4j
@Component
public class JwtTokenFilter extends OncePerRequestFilter {

	private final AuthServiceClientPort authServiceClient;

	private boolean jwtAuthEnabled;

	public JwtTokenFilter(AuthServiceClientPort authServiceClient,
			@Value("${jwt.auth.enabled}") boolean jwtAuthEnabled) {
		this.authServiceClient = authServiceClient;
		this.jwtAuthEnabled = jwtAuthEnabled;
	}

	/**
	 * 用於進行 URL 路徑匹配的 Ant 格式路徑匹配器。
	 */
	private final AntPathMatcher pathMatcher = new AntPathMatcher();

	/**
	 * 公開的路徑列表，不需要進行 JWT 驗證的路徑。
	 */
	private static final String[] PUBLIC_PATHS = { "/health", "/favicon.ico", "**/api-docs/**", "**/swagger-ui**",
			"/api/v1/ticket", "/swagger*", "/swagger-ui/*", "/api/v1/train**", "/api/v1/train/**", "/api/v1/seats/**",
			"/api/v1/options/**", "/api/v1/account/**", "/actuator/**", "/v3/api-docs/**", "/api/v1/login",
			"/api/v1/settings/**" };

	/**
	 * 過濾器的核心方法，用於處理 Request 中的授權資訊。
	 * 
	 * @param request  HTTP 請求
	 * @param response HTTP 回應
	 * @param chain    過濾器鏈
	 * @throws ServletException 如果發生 Servlet 相關異常
	 * @throws IOException      如果發生 I/O 相關異常
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {

		log.debug("AuthenticationTokenFilter doFilterInternal");

		// 如果該 Request 不需要 JWT 驗證，則直接放行
		if (!requiresJwtValidation(request)) {
			chain.doFilter(request, response);
			return;
		}

		// 未開直接放行
		if (!jwtAuthEnabled) {
			JwtTokenValidatedAndParsedResource jwtContext = new JwtTokenValidatedAndParsedResource(
					"nick123@example.com", new ArrayList<>());
			// 設置資料進 ContextHolder 供後續取用
			this.setJwtTokenContext("Test", jwtContext);
			chain.doFilter(request, response);
			return;
		}

		// 從 Request Header 中獲取授權資訊
		String authorization = request.getHeader(JwtConstants.JWT_HEADER.getValue());
		log.info("request header authorization: {}", authorization);

		// 如果授權資訊不為空且以 Bearer 開頭
		if (authorization != null && authorization.startsWith(JwtConstants.JWT_PREFIX.getValue())) {
			// 截取 Bearer 後面的 Access Token
			final String token = authorization.substring(JwtConstants.JWT_PREFIX.getValue().length());
			ContextHolder.setJwtToken(token); // 設置 Jwt Token 進入上下文工具

			if (validateJwtToken(token, response)) {
				chain.doFilter(request, response);
			}

		} else {
			log.error("No JWT Token");
			// 如果沒有獲取到授權資訊，返回 401 錯誤碼
			throw new ValidationException("401", "無權限執行該行為");
		}
	}

	/**
	 * 根據 Request 的 URL 判斷是否需要進行 JWT 驗證。
	 * 
	 * @param request HTTP 請求
	 * @return 是否需要進行 JWT 驗證
	 */
	private boolean requiresJwtValidation(HttpServletRequest request) {
		String requestPath = request.getRequestURI();
		// 檢查 Request 的 URL 是否在公開路徑列表中，如果是則不需要進行 JWT 驗證
		for (String publicPath : PUBLIC_PATHS) {
			log.info("publicPath:{}, requestPath:{}", publicPath, requestPath);
			if (pathMatcher.match(publicPath, requestPath)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 驗證 JWT Token，並在驗證成功後將 Token 存儲到 ContextHolder 中。
	 * 
	 * @param token    JWT Token
	 * @param response HTTP 回應
	 * @return 驗證結果，驗證成功返回 true，否則返回 false
	 * @throws IOException 如果發生 I/O 相關異常
	 */
	private boolean validateJwtToken(String token, HttpServletResponse response) throws IOException {
		// 呼叫外部 API 來驗證並解析 JWT Token
		try {
			JwtTokenValidatedAndParsedResource tokenBody = authServiceClient.validateJwtToken(token);
			log.debug("JWT Parsed claims: {}", tokenBody);

			// 驗證通過將 JWT Token 存儲到 ContextHolder 中
			this.setJwtTokenContext(token, tokenBody);
		} catch (Exception e) {
			log.error("發生錯誤", e);
			return false;
		}

		return true;

	}

	/**
	 * 設置資料進 ContextHolder 供後續取用
	 * 
	 * @param jwtContext 解析後的資料
	 */
	private void setJwtTokenContext(String token, JwtTokenValidatedAndParsedResource jwtContext) {
		ContextHolder.setUsername(jwtContext.getUsername());
		ContextHolder.setRoleList(jwtContext.getRoles());
		ContextHolder.setEmail(jwtContext.getUsername()); // 這邊設計帳號與使用者名稱相同
	}
}
