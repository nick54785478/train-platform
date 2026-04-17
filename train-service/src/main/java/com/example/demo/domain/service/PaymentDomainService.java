package com.example.demo.domain.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.example.demo.base.shared.enums.YesNo;
import com.example.demo.base.shared.exception.exception.ValidationException;
import com.example.demo.domain.account.aggregate.MoneyAccount;
import com.example.demo.domain.booking.aggregate.TicketBooking;
import com.example.demo.domain.booking.aggregate.vo.BookingStatus;

@Service
public class PaymentDomainService {

	/**
	 * 執行核心支付業務 這裡不涉及資料庫存取與 Outbox，只處理業務規則與狀態變更
	 */
	public void executePayment(TicketBooking booking, MoneyAccount account, BigDecimal amount, String txId) {

		// 1. 業務規則檢查：只有有效的訂單可以扣款 (這屬於領域知識)
		if (booking.getStatus() == BookingStatus.FAILED || booking.getActiveFlag() == YesNo.N) {
			throw new ValidationException("VALIDATE_FAILED", "訂單已失效，無法執行支付行為");
		}

		// 2. 執行扣款
		account.charge(amount, booking.getUuid(), txId);

		// 3. 變更訂單狀態
		booking.onPaid();
	}
}