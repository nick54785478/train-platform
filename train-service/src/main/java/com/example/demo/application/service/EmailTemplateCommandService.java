package com.example.demo.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.base.application.service.BaseApplicationService;
import com.example.demo.base.shared.enums.YesNo;
import com.example.demo.base.shared.exception.exception.ValidationException;
import com.example.demo.domain.email.aggregate.EmailTemplate;
import com.example.demo.domain.email.command.CreateEmailTemplateCommand;
import com.example.demo.domain.email.command.SaveEmailTemplateCommand;
import com.example.demo.infra.repository.EmailTemplateRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class EmailTemplateCommandService extends BaseApplicationService {

	private final EmailTemplateRepository repository;

	/**
	 * 新增範本
	 * 
	 * @param command {@link CreateEmailTemplateCommand}
	 */
	public void createTemplate(CreateEmailTemplateCommand command) {
		// 檢查 Key 是否重複
		repository.findByTemplateKey(command.getTemplateKey()).ifPresent(t -> {
			throw new ValidationException("VALIDATE_EXCEPTION", "範本識別碼已存在: " + command.getTemplateKey());
		});

		EmailTemplate template = EmailTemplate.create(command.getTemplateKey(), command.getName(), command.getSubject(),
				command.getContent());
		repository.save(template);
	}

	/**
	 * 修改範本
	 */
	public void updateTemplate(Long id, SaveEmailTemplateCommand command) {
		EmailTemplate template = repository.findById(id).orElseThrow(() -> new RuntimeException("找不到該範本"));

		template.update(command.getSubject(), command.getContent(), YesNo.valueOf(command.getActiveFlag()));
		repository.save(template);
	}

	/**
	 * 刪除範本 (邏輯刪除)
	 */
	public void deleteTemplate(Long id) {
		repository.findById(id).ifPresent(template -> {
			template.disable();
			repository.save(template);
		});
	}
}