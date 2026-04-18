package com.example.demo.iface.rest;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.application.service.MoneyAccountCommandService;
import com.example.demo.application.service.MoneyAccountQueryService;
import com.example.demo.domain.account.command.CreateMoneyAccountCommand;
import com.example.demo.domain.account.command.DepositMoneyCommand;
import com.example.demo.iface.dto.req.CreateMoneyAccountResource;
import com.example.demo.iface.dto.req.DepositMoneyResource;
import com.example.demo.iface.dto.res.MoneyAccountQueriedResource;
import com.example.demo.iface.dto.res.MoneyAccountRegisteredResource;
import com.example.demo.iface.dto.res.MoneyDepositedResource;
import com.example.demo.util.BaseDataTransformer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/account")
@Tag(name = "Money Account API", description = "進行與儲值帳戶領域相關動作")
public class MoneyAccountController {

	private MoneyAccountQueryService moneyAccountQueryService;
	private MoneyAccountCommandService moneyAccountCommandService;

	/**
	 * 註冊使用者儲值帳號
	 * 
	 * @param resource
	 * @return 成功訊息
	 */
	@PostMapping("/register")
	@Operation(summary = "API - 註冊使用者儲值帳號", description = "註冊使用者儲值帳號。")
	public ResponseEntity<MoneyAccountRegisteredResource> register(
			@Parameter(description = "使用者儲值帳號資訊") @RequestBody CreateMoneyAccountResource resource) {
		// DTO 轉換
		CreateMoneyAccountCommand command = BaseDataTransformer.transformData(resource,
				CreateMoneyAccountCommand.class);
		moneyAccountCommandService.register(command);
		return new ResponseEntity<>(new MoneyAccountRegisteredResource("200", "註冊成功"), HttpStatus.OK);
	}

	/**
	 * 進行儲值動作
	 * 
	 * @param resource
	 * @return 成功訊息
	 */
	@PostMapping("/deposit")
	@Operation(summary = "API - 進行儲值動作", description = "進行儲值動作。")
	public ResponseEntity<MoneyDepositedResource> deposit(
			@Parameter(description = "儲值資訊") @RequestBody DepositMoneyResource resource) {

		// 在 API 入口生成這次交易的唯一追蹤碼 (Saga TX ID)
		String sagaTxId = UUID.randomUUID().toString();
		log.info("API 接收到儲值請求 - TX_ID: {}, Resource: {}", sagaTxId, resource);
		DepositMoneyCommand command = BaseDataTransformer.transformData(resource, DepositMoneyCommand.class);
		moneyAccountCommandService.regularDeposit(command, sagaTxId);
		return ResponseEntity.ok(new MoneyDepositedResource("200", "儲值成功"));

	}

	/**
	 * 透過使用者帳號查詢儲值帳號資訊
	 * 
	 * @param username
	 * @return 帳戶資訊
	 */
	@GetMapping("")
	@Operation(summary = "API - 透過使用者帳號查詢儲值帳號資訊", description = "透過使用者帳號查詢儲值帳號資訊。")
	public ResponseEntity<MoneyAccountQueriedResource> queryAccount(@RequestParam String username) {
		return new ResponseEntity<>(BaseDataTransformer.transformData(moneyAccountQueryService.queryAccount(username),
				MoneyAccountQueriedResource.class), HttpStatus.OK);
	}

}
