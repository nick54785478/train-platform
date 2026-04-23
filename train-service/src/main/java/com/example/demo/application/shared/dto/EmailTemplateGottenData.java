package com.example.demo.application.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailTemplateGottenData {

	private Long id;

	private String templateKey;

	private String templateName;

	private String subject;

	private String content;

	private String activeFlag;
}
