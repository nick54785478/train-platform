package com.example.demo.service;

import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.base.shared.enums.YesNo;
import com.example.demo.base.shared.exception.exception.ValidationException;
import com.example.demo.domain.customisation.aggregate.Customisation;
import com.example.demo.domain.customisation.aggregate.vo.CustomisationType;
import com.example.demo.domain.customisation.command.CreateCustomisationCommand;
import com.example.demo.domain.customisation.command.UpdateCustomisationCommand;
import com.example.demo.domain.customisation.command.UpdateCustomizedValueCommand;
import com.example.demo.domain.service.CustomisationService;
import com.example.demo.infra.repository.CustomisationRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, timeout = 36000, rollbackFor = Exception.class)
public class CustomisationCommandService {

	private CustomisationService customissionService;
	private CustomisationRepository customisationRepository;

	/**
	 * 建立一筆個人化配置
	 * 
	 * @param command
	 */
	public void create(CreateCustomisationCommand command) {
		Customisation customission = new Customisation();
		customission.create(command);
		customisationRepository.save(customission);
	}
	
	/**
	 * 更新一筆個人化配置
	 * 
	 * @param command
	 */
	public void update(UpdateCustomisationCommand command) {
		Customisation customission = customisationRepository.findByUsernameAndTypeAndNameAndActiveFlag(
				command.getUsername(), CustomisationType.fromLabel(command.getType()), command.getName(),
				YesNo.valueOf(command.getActiveFlag()));
		if (Objects.isNull(customission)) {
			log.error("發生錯誤，更新失敗");
			throw new ValidationException("VALIDATION_FAILED", "發生錯誤，更新失敗");
		}
		customission.update(command);
		customisationRepository.save(customission);
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
