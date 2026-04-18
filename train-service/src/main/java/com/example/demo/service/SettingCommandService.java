package com.example.demo.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.base.application.service.BaseApplicationService;
import com.example.demo.base.shared.exception.exception.ValidationException;
import com.example.demo.domain.setting.aggregate.ConfigurableSetting;
import com.example.demo.domain.setting.command.CreateSettingCommand;
import com.example.demo.domain.setting.command.UpdateSettingCommand;
import com.example.demo.infra.repository.SettingRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, timeout = 36000, rollbackFor = Exception.class)
public class SettingCommandService extends BaseApplicationService {

	private SettingRepository settingRepository;

	/**
	 * 建立設定
	 * 
	 * @param command CreateSettingCommand
	 */
	public void create(CreateSettingCommand command) {
		// 檢查設定
		if (StringUtils.equals(command.getDataType(), "CONFIGURE") && command.getPriorityNo() != 0L) {
			throw new ValidationException("VALIDATE_FAILED", "資料配置有誤，Configure 的排序號需為 0");
		}

		if (StringUtils.equals(command.getDataType(), "DATA") && command.getPriorityNo() == 0L) {
			throw new ValidationException("VALIDATE_FAILED", "資料配置有誤，Data 的排序號需大於 0");
		}

		ConfigurableSetting setting = new ConfigurableSetting();
		setting.create(command);
		settingRepository.save(setting);

	}

	/**
	 * 修改設定
	 * 
	 * @param command
	 */
	public void update(UpdateSettingCommand command) {
		settingRepository.findById(command.getId()).ifPresentOrElse(setting -> {
			setting.update(command);
			settingRepository.save(setting);
		}, () -> {
			throw new ValidationException("VALIDATE_FAILED", "查無此資料，更新失敗");
		});
	}

	/**
	 * 刪除特定資料
	 * 
	 * @param id
	 */
	public void delete(Long id) {
		settingRepository.findById(id).ifPresentOrElse(setting -> {
			setting.delete();
			settingRepository.save(setting);
		}, () -> {
			log.error("查無此資料，ID:{}，刪除失敗", id);
			throw new ValidationException("VALIDATE_FAILED", "查無此資料，刪除失敗");
		});
	}
}
