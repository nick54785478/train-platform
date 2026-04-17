package com.example.demo.domain.account.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUserCommand {

	private String name; // 使用者名稱

	private String email; // 信箱

	private String username; // 帳號

	private String password; // 密碼

	private String address;	// 地址
}
