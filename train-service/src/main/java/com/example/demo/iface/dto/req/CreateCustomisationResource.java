package com.example.demo.iface.dto.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCustomisationResource {

	private String username;
	
	private String type; // 配置種類

	private String name;

	private String value;

	private String description; // 敘述
	
}
