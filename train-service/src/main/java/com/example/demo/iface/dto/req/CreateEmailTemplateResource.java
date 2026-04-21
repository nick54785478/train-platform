package com.example.demo.iface.dto.req;

import lombok.Data;

@Data
public class CreateEmailTemplateResource {
	
	private String templateKey; // Key 
	
	private String name;  // 範本名稱
	
	private String subject; // 標題
	
	private String content; // 內容
	
	private String activeFlag; // Y or N
}