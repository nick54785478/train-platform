package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.domain.service.CustomisationService;
import com.example.demo.domain.share.dto.CustomisationQueriedView;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
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
	public CustomisationQueriedView query(String username, String dataType, String type) {
		return customisationService.query(username, dataType, type);
	}
}
