package com.example.demo.application.port;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

import org.springframework.messaging.MessagingException;

import com.example.demo.infra.shared.command.SendMailCommand;

import jakarta.validation.Valid;

/**
 * 郵件發送器介面（Mail Sender Port）。
 *
 * <pre>
 * 本介面負責系統中所有郵件的發送功能，支援單一附件、多附件及 CC 多收件人的情境。 
 * 實現類別可基於 JavaMailSender、SMTP 或第三方郵件服務實作。
 * </pre>
 */
public interface MailSenderPort {

	/**
	 * 發送郵件。
	 * 
	 * @param to                 收件人電子郵件地址
	 * @param subject            郵件主題
	 * @param text               郵件內容
	 * @param attachmentFilename 附件檔案名稱
	 * @param file               附件檔案的輸入流
	 * @throws MessagingException              如果發送郵件過程中發生消息異常
	 * @throws IOException                     如果發送郵件過程中發生 IO 異常
	 * @throws jakarta.mail.MessagingException
	 */
	void send(@Valid SendMailCommand command) throws IOException, jakarta.mail.MessagingException;

	/**
	 * 發送郵件(含單個附件)。
	 * 
	 * @param to                 收件人電子郵件地址
	 * @param subject            郵件主題
	 * @param text               郵件內容
	 * @param attachmentFilename 附件檔案名稱
	 * @param file               附件檔案的輸入流
	 * @throws MessagingException              如果發送郵件過程中發生消息異常
	 * @throws jakarta.mail.MessagingException
	 * @throws IOException                     如果發送郵件過程中發生 IO 異常
	 * @throws jakarta.mail.MessagingException 
	 */
	default void send(String to, String subject, String text, String attachmentFilename, InputStream file)
			throws MessagingException, IOException, jakarta.mail.MessagingException {
		SendMailCommand command = SendMailCommand.builder().to(to).subject(subject).content(text)
				.attachments(attachmentFilename != null ? Map.of(attachmentFilename, file) : null).build();
		send(command);
	}

	/**
	 * 發送郵件(含多個附件)。
	 * 
	 * @param to      收件人電子郵件地址
	 * @param subject 郵件主題
	 * @param text    郵件內容
	 * @param map     Map<附件檔案名稱, 附件檔案的輸入流>
	 * @throws MessagingException              如果發送郵件過程中發生消息異常
	 * @throws jakarta.mail.MessagingException
	 * @throws IOException                     如果發送郵件過程中發生 IO 異常
	 * @throws jakarta.mail.MessagingException 
	 */
	default void send(String to, String subject, String text, Map<String, InputStream> attachments)
			throws MessagingException, IOException, jakarta.mail.MessagingException {
		SendMailCommand command = SendMailCommand.builder().to(to).subject(subject).content(text)
				.attachments(attachments).build();
		send(command);
	}

	/**
	 * 同一封郵件 CC 給多個對象。
	 * 
	 * @param to      收件者
	 * @param ccList  多個收件人電子郵件地址清單(以 "," 隔開)
	 * @param subject 郵件主題
	 * @param text    郵件內容
	 * @param map     Map<附件檔案名稱, 附件檔案的輸入流>
	 * @throws MessagingException              如果發送郵件過程中發生消息異常
	 * @throws jakarta.mail.MessagingException
	 * @throws IOException                     如果發送郵件過程中發生 IO 異常
	 * @throws jakarta.mail.MessagingException 
	 */
	default void sendAndCc(String to, String ccList, String subject, String text, Map<String, InputStream> attachments)
			throws MessagingException, IOException, jakarta.mail.MessagingException {
		SendMailCommand command = SendMailCommand.builder().to(to)
				.ccList(ccList != null ? Arrays.asList(ccList.split(",")) : null).subject(subject).content(text)
				.attachments(attachments).build();
		send(command);
	}
}
