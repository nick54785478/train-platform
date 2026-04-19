package com.example.demo.application.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.demo.application.port.MailSenderPort;
import com.example.demo.application.port.TemplateEnginePort;
import com.example.demo.base.shared.enums.YesNo;
import com.example.demo.infra.repository.EmailTemplateRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class NotificationService {

	private final MailSenderPort mailSender;
	private final TemplateEnginePort templateEngine;
	private final EmailTemplateRepository templateRepository;

	/**
	 * 開戶成功：歡迎信
	 */
	public void sendWelcomeEmail(String to, String username) {
		this.processAndSend(to, "WELCOME_EMAIL", Map.of("username", username));
	}

	/**
	 * 開戶失敗：補償通知
	 */
	public void sendAccountRegistrationFailureAlert(String email, String reason) {
		this.processAndSend(email, "ACCOUNT_REG_FAILURE", Map.of("reason", reason));
	}

	/**
	 * 訂票成功：確認信
	 */
	public void sendBookingSuccessEmail(String to, String bookingUuid) {
		this.processAndSend(to, "BOOKING_SUCCESS", Map.of("bookingUuid", bookingUuid));
	}

	/**
	 * 訂票最終失敗：退款/補償告知
	 */
	public void sendBookingFailureAlert(String to, String bookingUuid, String reason) {
		this.processAndSend(to, "BOOKING_SAGA_FAILURE", Map.of("bookingUuid", bookingUuid, "reason", reason));
	}

	/**
	 * 封裝核心流程：檢索 -> 渲染 -> 寄送
	 */
	private void processAndSend(String to, String templateKey, Map<String, Object> model) {
		templateRepository.findByTemplateKeyAndActiveFlag(templateKey, YesNo.Y).ifPresentOrElse(template -> {
			try {
				// 渲染主旨與內容 (標題也能動態化)
				String renderedSubject = templateEngine.renderFromString(templateKey + "_SUB", template.getSubject(),
						model);
				String renderedContent = templateEngine.renderFromString(templateKey + "_CNT", template.getContent(),
						model);

				mailSender.send(to, renderedSubject, renderedContent, null, null);
				log.info("Saga 通知成功寄送: [{}], 目標: {}", templateKey, to);

			} catch (Exception e) {
				log.error("通知渲染或發送異常 [{}]: {}", templateKey, e.getMessage());
			}
		}, () -> log.error("通知發送失敗：找不到對應範本 [{}]", templateKey));
	}
}
