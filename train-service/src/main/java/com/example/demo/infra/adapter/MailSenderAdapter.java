package com.example.demo.infra.adapter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import com.example.demo.application.port.MailSenderPort;
import com.example.demo.infra.shared.command.SendMailCommand;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 用於發送郵件的服務類。
 */
@Slf4j
@Component
@Validated
@RequiredArgsConstructor // 使用 lombok 自動注入 JavaMailSender
class MailSenderAdapter implements MailSenderPort {

	private final JavaMailSender javaMailSender;

	@Value("${spring.mail.username}")
	private String defaultFromAddress;

	@Value("${mail.default.from-name:火車票務系統}")
	private String defaultFromName;

	@Override
	public void send(SendMailCommand command) throws MessagingException, IOException {
		log.debug("準備發送郵件至: {}", command.getTo());

		MimeMessage msg = javaMailSender.createMimeMessage();
		// 第二個參數 true 代表支援 Multipart (附件/HTML)
		MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");

		// 1. 設置發件人 (優先使用 Command 指定的，否則用預設值)
		String from = StringUtils.hasText(command.getFrom()) ? command.getFrom() : defaultFromAddress;
		String name = StringUtils.hasText(command.getFromName()) ? command.getFromName() : defaultFromName;
		helper.setFrom(from, name);

		// 2. 設置收件人與主旨
		helper.setTo(command.getTo());
		helper.setSubject(command.getSubject());
		helper.setText(command.getContent(), true); // true 代表內容支援 HTML

		// 3. 設置 CC (處理 List 轉換)
		if (command.getCcList() != null && !command.getCcList().isEmpty()) {
			helper.setCc(command.getCcList().toArray(new String[0]));
		}

		// 4. 處理附件 (處理 Map)
		if (command.getAttachments() != null && !command.getAttachments().isEmpty()) {
			for (Map.Entry<String, InputStream> entry : command.getAttachments().entrySet()) {
				if (entry.getValue() != null) {
					byte[] content = IOUtils.toByteArray(entry.getValue());
					helper.addAttachment(entry.getKey(), new ByteArrayResource(content));
				}
			}
		}

		// 5. 正式發送
		this.javaMailSender.send(msg);
		log.info("郵件已成功寄出至: {}", command.getTo());
	}
}