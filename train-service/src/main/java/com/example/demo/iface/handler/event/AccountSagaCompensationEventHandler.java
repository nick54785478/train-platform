package com.example.demo.iface.handler.event;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.example.demo.application.service.BookingCommandService;
import com.example.demo.application.service.SeatCommandService;
import com.example.demo.base.iface.handler.BaseEventHandler;
import com.example.demo.domain.account.outbound.AccountDepositFailedEvent;

import lombok.extern.slf4j.Slf4j;

/**
 * 帳戶 Saga 補償事件處理器
 * <p>
 * 專門處理開戶流程（Saga）中的異常補償。 當初始儲值失敗時，此處理器會執行「凍結帳戶」動作。
 * </p>
 */
@Slf4j
@Component
@RabbitListener(queues = "${rabbitmq.account.saga.compensation}")
public class AccountSagaCompensationEventHandler extends BaseEventHandler {

	private final SeatCommandService seatCommandService;
	private final BookingCommandService bookingCommandService; // 協調者可以擁有多個 Application Service

	public AccountSagaCompensationEventHandler(SeatCommandService seatCommandService,
			BookingCommandService bookingCommandService) {
		this.seatCommandService = seatCommandService;
		this.bookingCommandService = bookingCommandService;
	}

	/**
	 * 處理初始儲值失敗事件 (補償路徑)
	 * 
	 * @param event {@link AccountDepositFailedEvent} 儲值失敗事件
	 */
	@RabbitHandler
	public void handleAccountDepositFailed(AccountDepositFailedEvent event) {
		log.warn("SAGA 補償啟動 - 準備進行跨服務協調：{}", event.getEventTxId());

		if (!this.checkEventIdempotency(event))
			return;

		// 1. 協調 Seat 服務：釋放資源
		// 此時 Seat 服務只做它份內的事
		seatCommandService.compensateCancel(event.getTargetId(), event.getEventTxId());

		// 2. 協調 Booking 服務：標記訂單失敗
		// 職責歸位：訂單的狀態由 Booking 服務自己改
		bookingCommandService.fail(event.getTargetId(), event.getEventTxId(), "初始儲值失敗，系統自動回滾資源");

		log.info("SAGA 補償路徑執行完畢 - 資源已釋放且訂單已標記失敗。");
	}
}