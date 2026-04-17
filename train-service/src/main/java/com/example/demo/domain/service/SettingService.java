package com.example.demo.domain.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.example.demo.base.shared.exception.exception.ValidationException;
import com.example.demo.domain.setting.aggregate.ConfigurableSetting;
import com.example.demo.domain.setting.command.CreateSettingCommand;
import com.example.demo.domain.setting.command.UpdateSettingCommand;
import com.example.demo.domain.share.SettingQueriedData;
import com.example.demo.infra.repository.SettingRepository;
import com.example.demo.util.BaseDataTransformer;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class SettingService {

	private SettingRepository settingRepository;

	/**
	 * 建立設定
	 * 
	 * @param command
	 */
	public void create(CreateSettingCommand command) {
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
	 * 根據條件查詢 Setting
	 * 
	 * @param dataType
	 * @param type
	 * @param name
	 * @param activeFlag
	 * @return
	 */
	public List<SettingQueriedData> query(String dataType, String type, String name, String activeFlag) {
		List<ConfigurableSetting> settingList = settingRepository.findAllWithSpecification(dataType, type, name,
				activeFlag);
		return BaseDataTransformer.transformData(settingList, SettingQueriedData.class);
	}

	/**
	 * 刪除特定 id 的 Setting 資料
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

	/**
	 * 檢查新增資料
	 * 
	 * @param command
	 */
	public void checkSetting(CreateSettingCommand command) {
		if (StringUtils.equals(command.getDataType(), "CONFIGURE") && command.getPriorityNo() != 0L) {
			throw new ValidationException("VALIDATE_FAILED", "資料配置有誤，Configure 的排序號需為 0");
		}

		if (StringUtils.equals(command.getDataType(), "DATA") && command.getPriorityNo() == 0L) {
			throw new ValidationException("VALIDATE_FAILED", "資料配置有誤，Data 的排序號需大於 0");
		}

	}

}
