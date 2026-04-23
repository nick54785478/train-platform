package com.example.demo.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.application.shared.dto.EmailTemplateGottenData;
import com.example.demo.base.application.service.BaseApplicationService;
import com.example.demo.base.shared.enums.YesNo;
import com.example.demo.base.shared.exception.exception.ValidationException;
import com.example.demo.domain.email.aggregate.EmailTemplate;
import com.example.demo.domain.setting.aggregate.ConfigurableSetting;
import com.example.demo.infra.repository.EmailTemplateRepository;
import com.example.demo.infra.repository.SettingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailTemplateQueryService extends BaseApplicationService {

	private final EmailTemplateRepository repository;
	private final SettingRepository settingRepository;

	/**
	 * 透過 Template Key 查詢特定 Template Key
	 * 
	 * @param templateKey 信件範本唯一值
	 * @return 信件範本資料
	 */
	public EmailTemplateGottenData getEmailTemplateByKey(String templateKey) {

		// 查詢是否為合法配置 (以目前的設計原則上只有一筆)
		ConfigurableSetting setting = settingRepository
				.findByDataTypeAndTypeAndActiveFlag("EMAIL_TEMPLATE", templateKey, YesNo.Y).stream().findFirst()
				.orElseThrow(() -> new ValidationException("VALIDATE_FAILED", "該 Template Key 為不合法的設定"));

		EmailTemplate template = repository.findByTemplateKey(templateKey).orElseGet(() -> {
			return EmailTemplate.create(templateKey, setting.getName(), null, null);
		});

		return this.transformData(template, EmailTemplateGottenData.class);
	}

	public List<EmailTemplate> findAll() {
		return repository.findAll();
	}

	public EmailTemplate findById(Long id) {
		return repository.findById(id).orElse(null);
	}

}