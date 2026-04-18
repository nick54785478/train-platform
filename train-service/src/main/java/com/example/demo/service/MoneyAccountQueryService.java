package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.application.shared.dto.MoneyAccountQueriedData;
import com.example.demo.base.application.service.BaseApplicationService;
import com.example.demo.base.shared.exception.exception.ValidationException;
import com.example.demo.domain.account.aggregate.MoneyAccount;
import com.example.demo.infra.repository.MoneyAccountRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class MoneyAccountQueryService extends BaseApplicationService {

	private MoneyAccountRepository moneyAccountRepository;

	/**
	 * 透過使用者帳號查詢儲值帳號資訊
	 * 
	 * @param username 使用者帳號
	 * @return 儲值帳號資訊
	 */
	public MoneyAccountQueriedData queryAccount(String username) {
		MoneyAccount account = moneyAccountRepository.findByUsername(username);

		if (account == null) {
			throw new ValidationException("VALIDATION_EXCEPTION", "查無此使用者資料");
		}

		return this.transformData(account, MoneyAccountQueriedData.class);
		
	}
}
