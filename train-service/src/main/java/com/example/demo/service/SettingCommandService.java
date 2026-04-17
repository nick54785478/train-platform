package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.base.application.service.BaseApplicationService;
import com.example.demo.domain.service.SettingService;
import com.example.demo.domain.setting.command.CreateSettingCommand;
import com.example.demo.domain.setting.command.UpdateSettingCommand;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, timeout = 36000, rollbackFor = Exception.class)
public class SettingCommandService extends BaseApplicationService {

	private SettingService settingService;

	/**
	 * 建立設定
	 * 
	 * @param command CreateSettingCommand
	 */
	public void create(CreateSettingCommand command) {
		// 檢查設定
		settingService.checkSetting(command);
		settingService.create(command);
	}

	/**
	 * 修改設定
	 * 
	 * @param command
	 */
	public void update(UpdateSettingCommand command) {
		// 檢查設定
		settingService.update(command);
	}

	/**
	 * 刪除特定資料
	 * 
	 * @param id
	 */
	public void delete(Long id) {
		settingService.delete(id);
	}
}
