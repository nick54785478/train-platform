package com.example.demo.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.domain.email.aggregate.EmailTemplate;
import com.example.demo.infra.repository.EmailTemplateRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EmailTemplateQueryService {

	private final EmailTemplateRepository repository;

	public List<EmailTemplate> findAll() {
		return repository.findAll();
	}

	public EmailTemplate findById(Long id) {
		return repository.findById(id).orElse(null);
	}
}