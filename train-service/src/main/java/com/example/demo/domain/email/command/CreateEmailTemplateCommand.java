package com.example.demo.domain.email.command;

import lombok.Data;

@Data
public class CreateEmailTemplateCommand {
	
	private String templateKey; // Key 
	
	private String name;  // 範本名稱
	
	private String subject; // 標題
	
	private String content; // 內容
	
	private String activeFlag; // Y or N
}