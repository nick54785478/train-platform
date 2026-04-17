package com.example.demo.iface.handler.event;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.base.iface.handler.BaseEventHandler;
import com.example.demo.domain.account.command.DepositMoneyCommand;
import com.example.demo.domain.booking.outbound.BookingCancelledEvent;
import com.example.demo.service.MoneyAccountCommandService;
import com.example.demo.service.SeatCommandService;

import lombok.extern.slf4j.Slf4j;

/**
 * Saga 處理器：處理訂單取消後的後續流程 (退款與釋放座位) 適用情境：1. 支付失敗後的自動補償 2. 使用者手動退票後的資源清理
 */
@Slf4j
@Component
@RabbitListener(queues = "${rabbitmq.booking.saga.cancel}")
public class BookingCancelSagaHandler extends BaseEventHandler {

	@Autowired
	private SeatCommandService seatCommandService;

	@Autowired
	private MoneyAccountCommandService moneyAccountCommandService;

	/**
	 * 處理 BookingCancelledEvent 這裡不呼叫 BookingCommandService，因為此事件發出時，訂單狀態通常已經由
	 * Booking 服務更新完畢
	 */
	@RabbitHandler
	public void handleCancel(BookingCancelledEvent event) {
		log.info("SAGA 取消流程 [開始] - 訂單: {}, Saga ID: {}", event.getBookingUuid(), event.getEventTxId());

		// 1. 冪等性檢查：防止重複執行退款或重複釋放座位
		if (!this.checkEventIdempotency(event)) {
			log.warn("SAGA 取消流程 - 事件已處理，忽略重複請求: {}", event.getEventTxId());
			return;
		}

		try {
			// 2. 第一步：釋放座位資源
			// 呼叫 Seat 服務的通用補償方法，解鎖該訂單佔用的物理座位
			log.info("SAGA 取消流程 - 執行步驟 1: 釋放座位");
			seatCommandService.compensateCancel(event.getBookingUuid(), event.getEventTxId());

			// 3. 第二步：退還款項
			// 建立儲值指令，將錢退回至 event 指定的 Account UUID
			log.info("SAGA 取消流程 - 執行步驟 2: 執行退款至帳戶 {}", event.getTargetId());
			DepositMoneyCommand refundCommand = DepositMoneyCommand.builder().uuid(event.getTargetId()) // Account UUID
					.money(event.getRefundAmount()).build();

			// 呼叫日常儲值 API 的邏輯完成退款
			moneyAccountCommandService.regularDeposit(refundCommand, event.getEventTxId());

			log.info("SAGA 取消流程 [成功] - 訂單 {} 的資源清理與退款已完成", event.getBookingUuid());

		} catch (Exception e) {
			// 如果處理過程中發生異常，拋出以觸發 MQ 重試機制
			log.error("SAGA 取消流程 [失敗] - 處理訂單 {} 時發生異常: {}", event.getBookingUuid(), e.getMessage());
			throw e;
		}
	}
}