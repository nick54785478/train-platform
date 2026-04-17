package com.example.demo.domain.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.demo.base.domain.service.BaseDomainService;
import com.example.demo.base.shared.enums.YesNo;
import com.example.demo.base.shared.exception.exception.ValidationException;
import com.example.demo.domain.setting.aggregate.ConfigurableSetting;
import com.example.demo.domain.share.SettingQueriedData;
import com.example.demo.domain.share.TemplateQueriedData;
import com.example.demo.domain.share.TimetableDetailGeneratedData;
import com.example.demo.domain.share.TimetableGeneratedData;
import com.example.demo.domain.template.aggregate.Template;
import com.example.demo.domain.template.aggregate.vo.TemplateType;
import com.example.demo.domain.train.command.GenerateTimetableCommand;
import com.example.demo.infra.repository.SettingRepository;
import com.example.demo.infra.repository.TemplateRepository;
import com.example.demo.util.BaseDataTransformer;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class TimetableService extends BaseDomainService {

	private SettingRepository settingRepository;
	private TemplateRepository templateRepository;

	/**
	 * 建立火車時刻表(起訖站)所需資料
	 * 
	 * @param commands 包含該起迄站火車車次資料
	 * @return TimetableGeneratedData
	 */
	public TimetableGeneratedData generateTimetableByFromStopAndToStop(List<GenerateTimetableCommand> commands) {
		// 取得火車時刻表範本資訊
		Template template = templateRepository.findByTypeAndDeleteFlag(TemplateType.TRAIN_TIMETABLE, YesNo.N);
		if (Objects.isNull(template)) {
			log.error("取得範本資訊失敗");
			throw new ValidationException("VALIDATE_FAILED", "取得範本資訊失敗");
		}
		TemplateQueriedData templateData = this.transformEntityToData(template, TemplateQueriedData.class);

		List<TimetableDetailGeneratedData> details = BaseDataTransformer.transformData(commands,
				TimetableDetailGeneratedData.class);
		
		log.debug("details:{}",details);

		// 取得 Parameters
		List<ConfigurableSetting> settings = settingRepository.findByDataTypeAndTypeAndActiveFlag("JASPER_PARAMETER",
				TemplateType.TRAIN_TIMETABLE.getCode(), YesNo.Y);
		
		List<SettingQueriedData> parameterList = this.transformEntityToData(settings, SettingQueriedData.class);
		Map<String, Object> parameters = parameterList.stream()
				.collect(Collectors.toMap(SettingQueriedData::getName, SettingQueriedData::getValue));
		System.out.println(parameters);
		// 設置 Detail 大小
		parameters.put("detailSize", details.size());

		TimetableGeneratedData timetableGeneratedData = new TimetableGeneratedData();
		timetableGeneratedData.setTemplateQueriedData(templateData);
		timetableGeneratedData.setDetails(details);
		timetableGeneratedData.setParameters(parameters);
		return timetableGeneratedData;
	}
}
