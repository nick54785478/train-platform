package com.example.demo.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.domain.service.CustomisationService;
import com.example.demo.domain.shared.dto.CustomisationQueriedView;

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
	 * @param dataType {@link ConfigurableSetting} 的配置種類
	 * @param type     設定類別
	 * @return 個人客製化設定
	 */
	public CustomisationQueriedView query(String username, String dataType, String type) {
		return customisationService.query(username, dataType, type);
	}
}
