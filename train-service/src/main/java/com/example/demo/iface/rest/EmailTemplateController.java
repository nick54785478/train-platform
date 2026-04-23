package com.example.demo.iface.rest;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.application.service.EmailTemplateCommandService;
import com.example.demo.application.service.EmailTemplateQueryService;
import com.example.demo.application.shared.dto.EmailTemplateGottenData;
import com.example.demo.domain.email.aggregate.EmailTemplate;
import com.example.demo.domain.email.command.SaveEmailTemplateCommand;
import com.example.demo.iface.dto.req.SaveEmailTemplateResource;
import com.example.demo.iface.dto.res.EmailTemplateSavedResource;
import com.example.demo.util.BaseDataTransformer;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/email-templates")
@RequiredArgsConstructor
public class EmailTemplateController {

	private final EmailTemplateCommandService commandService;
	private final EmailTemplateQueryService queryService;

	/**
	 * 取得單一範本配置 (By Template Key)
	 */
	@GetMapping("/{key}")
	public ResponseEntity<EmailTemplateGottenData> getEmailTemplateByKey(@PathVariable String key) {
		EmailTemplateGottenData template = queryService.getEmailTemplateByKey(key);
		return ResponseEntity.ok(template);
	}

	/**
	 * 取得所有範本清單
	 */
	@GetMapping
	public ResponseEntity<List<EmailTemplate>> list() {
		return ResponseEntity.ok(queryService.findAll());
	}

	/**
	 * 新增/修改範本
	 */
	@PostMapping
	public ResponseEntity<EmailTemplateSavedResource> create(@RequestBody SaveEmailTemplateResource resource) {
		SaveEmailTemplateCommand command = BaseDataTransformer.transformData(resource, SaveEmailTemplateCommand.class);
		commandService.saveTemplate(command);
		return ResponseEntity.ok(new EmailTemplateSavedResource("200", "新增/修改範本成功"));
	}

}