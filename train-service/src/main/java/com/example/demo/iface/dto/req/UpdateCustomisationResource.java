package com.example.demo.iface.dto.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCustomisationResource {
	
	private String username;
	
	private String type;
	
	private String name;
	
	private String value;

	private String description; // 敘述
	
	private String activeFlag;
	
}
