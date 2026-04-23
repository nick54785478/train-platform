package com.example.demo.iface.dto.req;

import lombok.Data;

@Data
public class SaveEmailTemplateResource {
	
	private String templateKey; // Key 
	
	private String templateName;  // 範本名稱
	
	private String subject; // 標題
	
	private String content; // 內容
	
}