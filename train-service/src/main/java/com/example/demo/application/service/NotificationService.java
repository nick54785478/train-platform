package com.example.demo.application.service;

import org.springframework.stereotype.Service;

import com.example.demo.application.port.MailSenderPort;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class NotificationService {

	private final MailSenderPort mailSender;
    /**
     * 發送開戶成功歡迎信
     */
    public void sendWelcomeEmail(String to, String username) {
        String subject = "【歡迎】您的帳戶已成功開通！";
        String text = String.format(
            "<html><body>" +
            "<h3>親愛的 %s 您好：</h3>" +
            "<p>恭喜您！您的帳戶開通申請已通過審核，現在您可以開始使用系統的完整功能（如訂票、儲值等）。</p>" +
            "<p>祝您使用愉快！</p>" +
            "</body></html>",
            username
        );

        try {
            mailSender.send(to, subject, text, null, null);
            log.info("歡迎信已寄送至：{}", to);
        } catch (Exception e) {
            log.error("歡迎信寄送失敗：{}", e.getMessage());
        }
    }
    
	/**
     * 發送開戶失敗警告信
     */
    public void sendAccountRegistrationFailureAlert(String email, String reason) {
        String subject = "【重要通知】開戶流程中斷提醒";
        
        // 這裡可以使用模板引擎 (如 Thymeleaf) 或簡單的 String.format
        String content = String.format(
            "<html><body>" +
            "<h3>您好：</h3>" +
            "<p>很抱歉通知您，您的帳戶開戶流程因以下原因中斷：<b>%s</b></p>" +
            "<p>目前帳戶已暫時凍結，請聯繫客服或重新嘗試。</p>" +
            "</body></html>", 
            reason
        );

        try {
            // 呼叫 Port 執行傳輸
            mailSender.send(email, subject, content, null, null);
            log.info("開戶失敗通知已成功寄送至：{}", email);
        } catch (Exception e) {
            // 寄信失敗不應導致 Saga 補償流程回滾，所以這裡通常捕獲異常並記錄
            log.error("郵件寄送失敗，Email: {}, 原因: {}", email, e.getMessage());
        }
    }
    
    /**
     * 發送訂票成功確認信
     */
    public void sendBookingSuccessEmail(String to, String bookingUuid) {
        String subject = "【確認通知】您已成功預訂車票！";
        String text = String.format(
            "<html><body>" +
            "<h3>訂位成功！</h3>" +
            "<p>您的車票訂購流程已全數完成，相關細節如下：</p>" +
            "<ul>" +
            "  <li>訂單編號：%s</li>" +
            "</ul>" +
            "<p>您可以登入系統查看您的車票詳細資訊與搭乘時間。</p>" +
            "<p>祝您旅途愉快！</p>" +
            "</body></html>",
            bookingUuid
        );

        try {
            mailSender.send(to, subject, text, null, null);
            log.info("訂票成功確認信已寄送至：{}", to);
        } catch (Exception e) {
            log.error("訂票成功郵件寄送異常：{}", e.getMessage());
        }
    }


    /**
     * 發送訂票 Saga 最終失敗通知
     * 註：如果 event 中沒帶 Email，則此處需要傳入 email
     */
    public void sendBookingFailureAlert(String to, String bookingUuid, String reason) {
        String subject = "【重要】訂票交易失敗通知";
        String text = String.format(
            "<html><body>" +
            "<h3>您好：</h3>" +
            "<p>很抱歉通知您，您的車票預訂動作（編號：%s）未能在系統中完成。</p>" +
            "<p><b>失敗原因：</b><span style='color: red;'>%s</span></p>" +
            "<p>若已涉及扣款，系統將會啟動自動退款流程。您可以稍後重新嘗試訂票。</p>" +
            "</body></html>",
            bookingUuid, reason
        );

        try {
            mailSender.send(to, subject, text, null, null);
            log.info("訂票失敗通知已寄送至：{}", to);
        } catch (Exception e) {
            log.error("訂票失敗通知寄送異常：{}", e.getMessage());
        }
    }
    
    
}
