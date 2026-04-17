package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.domain.customisation.command.CreateCustomisationCommand;
import com.example.demo.domain.customisation.command.UpdateCustomisationCommand;
import com.example.demo.domain.customisation.command.UpdateCustomizedValueCommand;
import com.example.demo.domain.service.CustomisationService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, timeout = 36000, rollbackFor = Exception.class)
public class CustomisationCommandService {

	private CustomisationService customissionService;

	/**
	 * 建立 個人化設定
	 * 
	 * @param command
	 */
	public void create(CreateCustomisationCommand command) {
		customissionService.create(command);
	}

	/**
	 * 更新 個人化設定
	 * 
	 * @param command
	 */
	public void update(UpdateCustomisationCommand command) {
		customissionService.update(command);
	}

	/**
	 * 更新該帳號對應的個人化設定
	 * 
	 * @param command
	 */
	public void updateCustomizedValue(UpdateCustomizedValueCommand command) {
		customissionService.updateCustomizedValue(command);
	}
}
