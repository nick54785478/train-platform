package com.example.demo.service;

import org.springframework.stereotype.Service;

import com.example.demo.domain.service.TemplateService;
import com.example.demo.domain.share.TemplateQueriedData;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class TemplateQueryService {

	private TemplateService templateService;

	/**
	 * 根據範本種類條件查詢
	 * 
	 * @param type
	 * @return TemplateQueriedData
	 */
	public TemplateQueriedData queryByType(String type) {
		return templateService.queryByType(type);
	}

}
