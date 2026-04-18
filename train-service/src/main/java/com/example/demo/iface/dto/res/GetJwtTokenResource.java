package com.example.demo.iface.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "UserLoginResource", description = "登入資訊")
public class UserLoginResource {

	@Schema(description = "帳號")
	private String username;

	@Schema(description = "密碼")
	private String password;
}
