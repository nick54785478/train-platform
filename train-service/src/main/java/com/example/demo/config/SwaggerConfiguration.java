package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfiguration {

	/**
	 * 定義基本的 OpenAPI 規格。
	 * 
	 * @return OpenAPI 物件，包含了 API 文件的基本資訊、安全配置等。
	 */
	@Bean
	public OpenAPI baseOpenAPI() {

		// 定義 API 文檔的基本資訊
		Info info = new Info().title("Train Demo API Document").version("v0.0.1")
				.description("""
						Train Demo API Document 包含了所有與我的範例系統相關的端點和操作。
							帳號: nick123@example.com
							密碼: password123
						""");
		// 創建一個 Components 物件，用於定義各種元件，如安全方案、請求體等
		Components components = new Components();

		// 定義名為 "bearerAuth" 的安全方案
		final String securitySchemeName = "bearerAuth";
		SecurityScheme securityScheme = new SecurityScheme().type(SecurityScheme.Type.HTTP) // 使用 HTTP 類型的認證
				.scheme("bearer") // 認證方案為 "bearer"
				.bearerFormat("JWT"); // 使用 JWT 格式的權杖

		// 將安全方案加入 OpenAPI 規格中的 components 部分
		components.addSecuritySchemes(securitySchemeName, securityScheme);

		// 定義一個安全要求，將上述的 "bearerAuth" 安全方案添加到要求中
		SecurityRequirement securityRequirement = new SecurityRequirement();
		securityRequirement.addList(securitySchemeName);

		// 創建 OpenAPI 物件，將以上定義的元件、安全要求和基本資訊添加到文檔中
		return new OpenAPI().components(components).addSecurityItem(securityRequirement).info(info)// 建立外部文檔
				.externalDocs(new ExternalDocumentation()
						.description("來源")  // 敘述
						.url("https://github.com/nick54785478/train-demo")  // 連接url
						);
	}

}
