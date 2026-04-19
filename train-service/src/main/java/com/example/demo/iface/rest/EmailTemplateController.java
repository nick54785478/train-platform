package com.example.demo.iface.rest;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.application.service.EmailTemplateCommandService;
import com.example.demo.application.service.EmailTemplateQueryService;
import com.example.demo.domain.email.aggregate.EmailTemplate;
import com.example.demo.domain.email.command.SaveEmailTemplateCommand;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/email-templates")
@RequiredArgsConstructor
public class EmailTemplateController {

	private final EmailTemplateCommandService commandService;
	private final EmailTemplateQueryService queryService;

	/**
	 * 取得所有範本清單
	 */
	@GetMapping
	public ResponseEntity<List<EmailTemplate>> list() {
		return ResponseEntity.ok(queryService.findAll());
	}

	/**
	 * 取得單一範本詳情
	 */
	@GetMapping("/{id}")
	public ResponseEntity<EmailTemplate> get(@PathVariable Long id) {
		return ResponseEntity.ok(queryService.findById(id));
	}

	/**
	 * 新增範本
	 */
	@PostMapping
	public ResponseEntity<Void> create(@RequestBody SaveEmailTemplateCommand command) {
		commandService.createTemplate(command);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	/**
	 * 更新範本
	 */
	@PutMapping("/{id}")
	public ResponseEntity<Void> update(@PathVariable Long id, @RequestBody SaveEmailTemplateCommand command) {
		commandService.updateTemplate(id, command);
		return ResponseEntity.ok().build();
	}

	/**
	 * 刪除範本
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		commandService.deleteTemplate(id);
		return ResponseEntity.noContent().build();
	}
}