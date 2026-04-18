package com.example.demo.base.shared.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetJwtTokenCommand {

	private String username;
	
	private String password;
}
