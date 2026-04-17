package com.example.demo.iface.dto.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterUserResource {

	private String name;

	private String email; // 信箱

	private String username; // 帳號

	private String password; // 密碼

	private String address;	// 地址
}
