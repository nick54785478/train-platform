package com.example.demo.base.shared.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtTokenValidatedAndParsedResource {

	private String username;
	
	private List<String> roles;
}
