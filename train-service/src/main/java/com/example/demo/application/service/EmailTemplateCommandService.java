package com.example.demo.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.base.application.service.BaseApplicationService;
import com.example.demo.domain.email.aggregate.EmailTemplate;
import com.example.demo.domain.email.command.SaveEmailTemplateCommand;
import com.example.demo.infra.repository.EmailTemplateRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class EmailTemplateCommandService extends BaseApplicationService {

	private final EmailTemplateRepository repository;

	/**
	 * 新增/修改範本
	 * 
	 * @param command {@link SaveEmailTemplateCommand}
	 */
	public void saveTemplate(SaveEmailTemplateCommand command) {
		// 檢查 Key 是否重複
		repository.findByTemplateKey(command.getTemplateKey()).ifPresentOrElse(template -> {
			template.update(command.getSubject(), command.getContent());
			repository.save(template);
		}, () -> {
			EmailTemplate template = EmailTemplate.create(command.getTemplateKey(), command.getTemplateName(),
					command.getSubject(), command.getContent());
			repository.save(template);
		});
		;

	}

}