package com.example.demo.domain.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.example.demo.base.domain.service.BaseDomainService;
import com.example.demo.base.infra.context.ContextHolder;
import com.example.demo.base.shared.enums.YesNo;
import com.example.demo.base.shared.exception.exception.ValidationException;
import com.example.demo.domain.customisation.aggregate.Customisation;
import com.example.demo.domain.customisation.aggregate.vo.CustomisationType;
import com.example.demo.domain.customisation.command.UpdateCustomizedValueCommand;
import com.example.demo.domain.setting.aggregate.ConfigurableSetting;
import com.example.demo.domain.shared.dto.CustomisationDetailView;
import com.example.demo.domain.shared.dto.CustomisationQueriedView;
import com.example.demo.infra.repository.CustomisationRepository;
import com.example.demo.infra.repository.SettingRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class CustomisationService extends BaseDomainService {

	private SettingRepository settingRepository;
	private CustomisationRepository customisationRepository;

//	/**
//	 * 建立一筆個人化配置
//	 * 
//	 * @param command
//	 */
//	public void create(CreateCustomisationCommand command) {
//		Customisation customission = new Customisation();
//		customission.create(command);
//		customisationRepository.save(customission);
//	}

//	/**
//	 * 更新一筆個人化配置
//	 * 
//	 * @param command
//	 */
//	public void update(UpdateCustomisationCommand command) {
//		Customisation customission = customisationRepository.findByUsernameAndTypeAndNameAndActiveFlag(
//				command.getUsername(), CustomisationType.fromLabel(command.getType()), command.getName(),
//				YesNo.valueOf(command.getActiveFlag()));
//		if (Objects.isNull(customission)) {
//			log.error("發生錯誤，更新失敗");
//			throw new ValidationException("VALIDATION_FAILED", "發生錯誤，更新失敗");
//		}
//		customission.update(command);
//		customisationRepository.save(customission);
//	}

	/**
	 * 查詢個人的表格顯示欄位設定
	 * 
	 * @param username
	 * @param type
	 * @return CustomissionQueriedData
	 */
	public CustomisationQueriedView query(String username, String dataType, String type) {
		List<CustomisationDetailView> data = new ArrayList<>();
		Customisation customisation = customisationRepository.findByUsernameAndTypeAndActiveFlag(username,
				CustomisationType.valueOf(type), YesNo.Y);

		Map<String, ConfigurableSetting> transformMap = settingRepository
				.findByDataTypeAndTypeAndActiveFlag(dataType, type, YesNo.Y).stream()
				.collect(Collectors.toMap(ConfigurableSetting::getName, Function.identity()));

		// 若無個人化配置，回傳全部
		if (Objects.isNull(customisation)) {
			data.addAll(settingRepository.findByDataTypeAndTypeAndActiveFlag(dataType, type, YesNo.Y).stream()
					.map(setting -> new CustomisationDetailView(setting.getId(), setting.getName(), setting.getValue()))
					.collect(Collectors.toList()));
		} else {
//			data = Stream.of(customisation.getValue().split(",")).map(e -> {
//				if (transformMap.containsKey(StringUtils.trim(e))) {
//					ConfigurableSetting setting = transformMap.get(StringUtils.trim(e));
//					return new OptionQueriedData(setting.getId(), setting.getName(), setting.getValue());
//				} else {
//					return null;
//				}
//			}).collect(Collectors.toList());
			data.addAll(Stream.of(customisation.getValue().split(",")).map(e -> transformMap.get(StringUtils.trim(e))) // 先找出設定值
					.filter(Objects::nonNull) // 過濾掉 null
					.map(setting -> new CustomisationDetailView(setting.getId(), setting.getName(), setting.getValue())) // 建立物件
					.collect(Collectors.toList()));
		}
		return new CustomisationQueriedView(username, data);
	}

	/**
	 * 更新該使用者的個人設定
	 * 
	 * @param command
	 */
	public void updateCustomizedValue(UpdateCustomizedValueCommand command) {
		List<ConfigurableSetting> settings = settingRepository.findByDataTypeAndTypeAndActiveFlag(command.getDataType(),
				command.getType(), YesNo.Y);
		Customisation customisation = customisationRepository.findByUsernameAndTypeAndActiveFlag(command.getUsername(),
				CustomisationType.valueOf(command.getType()), YesNo.Y);
		// 檢查是否為合法的設定
		this.checkValidCustomizedValue(settings, command);

		// 將 List<String> 轉為 "," 分隔的字串
		String value = String.join(",", command.getValueList());

		// 若查無個人配置，代表尚未配置 => 將進行個人化配置
		if (Objects.isNull(customisation)) {
			customisation = new Customisation();
			customisation.create(ContextHolder.getUsername(), command.getType(), command.getType(), value);
		} else {
			// 否則，進行更新個人化設定的值
			customisation.update(value);
		}
		customisationRepository.save(customisation);

	}

	/***
	 * 檢查是否為合法的設定
	 * 
	 * @param settings
	 * @param command
	 */
	private void checkValidCustomizedValue(List<ConfigurableSetting> settings, UpdateCustomizedValueCommand command) {
		// 取出所有設定中的 Name
		List<String> names = settings.stream().map(ConfigurableSetting::getName).collect(Collectors.toList());

		// 若該個人化設定中的值有不存在於 Setting 中的值，代表傳入的個人化設定的值有誤
		boolean hasInvalidValue = command.getValueList().stream().anyMatch(e -> !names.contains(e));

		if (hasInvalidValue) {
			throw new ValidationException("VALIDATE_FAILED", "該個人化設定有不合法的設定");
		}
	}
}
