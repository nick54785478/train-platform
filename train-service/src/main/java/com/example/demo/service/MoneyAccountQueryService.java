package com.example.demo.service;

import org.springframework.stereotype.Service;

import com.example.demo.base.application.service.BaseApplicationService;
import com.example.demo.domain.service.MoneyAccountService;
import com.example.demo.domain.share.MoneyAccountQueriedData;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class MoneyAccountQueryService extends BaseApplicationService {

	private MoneyAccountService moneyAccountService;

	/**
	 * 透過使用者帳號查詢儲值帳號資訊
	 * 
	 * @param username 使用者帳號
	 * @return 儲值帳號資訊
	 */
	public MoneyAccountQueriedData queryAccount(String username) {
		return moneyAccountService.queryAccount(username);
	}
}
