package com.example.demo.domain.email.command;

import lombok.Data;

@Data
public class SaveEmailTemplateCommand {
	
	private String templateKey; // Key 
	
	private String templateName;  // 範本名稱
	
	private String subject; // 標題
	
	private String content; // 內容
	
}