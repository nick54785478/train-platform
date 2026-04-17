package com.example.demo.domain.service;

import org.springframework.stereotype.Service;

import com.example.demo.base.domain.service.BaseDomainService;
import com.example.demo.base.shared.exception.exception.ValidationException;
import com.example.demo.domain.account.aggregate.MoneyAccount;
import com.example.demo.domain.share.MoneyAccountQueriedData;
import com.example.demo.infra.repository.MoneyAccountRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class MoneyAccountService extends BaseDomainService {

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

		return this.transformEntityToData(account, MoneyAccountQueriedData.class);
	}

}
