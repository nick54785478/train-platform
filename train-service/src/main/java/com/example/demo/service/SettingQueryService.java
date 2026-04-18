package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.application.shared.dto.SettingQueriedData;
import com.example.demo.base.application.service.BaseApplicationService;
import com.example.demo.domain.setting.aggregate.ConfigurableSetting;
import com.example.demo.infra.repository.SettingRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class SettingQueryService extends BaseApplicationService {

	private SettingRepository settingRepository;

	/**
	 * 根據條件查詢 Setting
	 * 
	 * @param dataType
	 * @param type
	 * @param name
	 * @param activeFlag
	 * @return List<SettingQueriedData>
	 */
	public List<SettingQueriedData> query(String dataType, String type, String name, String activeFlag) {
		List<ConfigurableSetting> settingList = settingRepository.findAllWithSpecification(dataType, type, name,
				activeFlag);
		return this.transformData(settingList, SettingQueriedData.class);
	}

}
