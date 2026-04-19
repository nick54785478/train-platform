package com.example.demo.infra.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.base.shared.enums.YesNo;
import com.example.demo.domain.email.aggregate.EmailTemplate;

public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {
	
	/**
	 * 透過識別碼與狀態查詢範本
	 */
	Optional<EmailTemplate> findByTemplateKeyAndActiveFlag(String templateKey, YesNo activeFlag);

	Optional<EmailTemplate> findByTemplateKey(String templateKey);
}