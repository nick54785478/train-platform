package com.example.demo.iface.dto.req;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "註冊使用者儲值帳號資訊")
public class CreateMoneyAccountResource {

	@Schema(description = "使用者名稱")
	private String name;	// 使用者名稱

	@Schema(description = "信箱")
	private String email; // 信箱

	@Schema(description = "帳號")
	private String username; // 帳號

	@Schema(description = "密碼")
	private String password; // 密碼

	@Schema(description = "地址")
	private String address;	// 地址
	
	@Schema(description = "金額")
	private BigDecimal money;
}
