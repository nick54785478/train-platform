package com.example.demo.infra.shared.command;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SendMailCommand {
	private String from; // 選填：特定發件人

	private String fromName; // 選填：發件人顯示名稱

	private String to; // 收件人電子郵件地址

	private String subject; // 郵件主題

	private String content; // 郵件內容

	private Map<String, InputStream> attachments; // Map<附件檔案名稱, 附件檔案的輸入流>

	private List<String> ccList; // CC 清單
}