package com.example.demo.domain.email.command;

import lombok.Data;

@Data
public class SaveEmailTemplateCommand {
	private String templateKey;
	private String subject;
	private String content;
	private String activeFlag; // Y or N
}