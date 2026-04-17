package com.example.demo.service;

import org.springframework.stereotype.Service;

import com.example.demo.domain.service.CustomisationService;
import com.example.demo.domain.share.CustomisationQueriedData;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CustomisationQueryService {

	private CustomisationService customisationService;

	/**
	 * 查詢個人客製化設定
	 * 
	 * @param username 使用者名稱
	 * @param dataType 
	 * @param type     設定類別
	 * @return CustomissionQueriedData
	 */
	public CustomisationQueriedData query(String username,String dataType, String type) {
		return customisationService.query(username, dataType, type);
	}
}
