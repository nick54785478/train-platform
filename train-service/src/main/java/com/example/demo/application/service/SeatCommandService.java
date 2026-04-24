package com.example.demo.application.service;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.base.application.service.BaseApplicationService;
import com.example.demo.base.domain.aggregate.DomainEvent;
import com.example.demo.domain.booking.aggregate.TicketBooking;
import com.example.demo.domain.seat.aggregate.TrainSeat;
import com.example.demo.domain.seat.command.BookSeatCommand;
import com.example.demo.domain.seat.command.CancelSeatByUserCommand;
import com.example.demo.domain.seat.command.CancelSeatCompensationCommand;
import com.example.demo.domain.seat.command.CheckInSeatCommand;
import com.example.demo.domain.seat.outbound.SeatBookedFailedEvent;
import com.example.demo.domain.shared.enums.PayMethod;
import com.example.demo.domain.ticket.aggregate.Ticket;
import com.example.demo.infra.repository.MoneyAccountRepository;
import com.example.demo.infra.repository.TicketBookingRepository;
import com.example.demo.infra.repository.TicketRepository;
import com.example.demo.infra.repository.TrainSeatRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SeatCommandService extends BaseApplicationService {

	private final TicketRepository ticketRepository;
	private final MoneyAccountRepository moneyAccountRepository;
	private final TrainSeatRepository trainSeatRepository;
	private final TicketBookingRepository ticketBookingRepository;
	private final BookingCommandService bookingCommandService;
	private final SeatCommandService self;

	public SeatCommandService(TicketRepository ticketRepository, MoneyAccountRepository moneyAccountRepository,
			TrainSeatRepository trainSeatRepository, TicketBookingRepository ticketBookingRepository,
			BookingCommandService bookingCommandService, @Lazy SeatCommandService self) {
		this.ticketRepository = ticketRepository;
		this.moneyAccountRepository = moneyAccountRepository;
		this.trainSeatRepository = trainSeatRepository;
		this.ticketBookingRepository = ticketBookingRepository;
		this.bookingCommandService = bookingCommandService;
		this.self = self;
	}

	/**
	 * 進行劃位動作
	 * 
	 * @param command   {@link CreateSeatCommand}
	 * @param eventTxId 該業務所屬唯一值
	 * @param payMethod 付費方式
	 * @param amount    金額
	 */
	@Transactional
	@Retryable(retryFor = { CannotAcquireLockException.class }, maxAttempts = 3, backoff = @Backoff(delay = 100))
	public void bookSeat(BookSeatCommand command, String eventTxId, String payMethod, BigDecimal amount) {

		try {

			TicketBooking booking = ticketBookingRepository.findById(command.getBookingUuid())
					.orElseThrow(() -> new RuntimeException("Booking not found"));

			moneyAccountRepository.findById(booking.getAccountUuid())
					.orElseThrow(() -> new RuntimeException("Account not found"));

			if (StringUtils.equals(PayMethod.fromLabel(payMethod).getCode(), PayMethod.PAY_BY_ACCOUNT.getCode())) {

				// 1. 建立 Seat（核心動作）& 註冊 Domain Event : SeatBookedEvent
				TrainSeat seat = TrainSeat.create(command, eventTxId, booking.getAccountUuid(), amount);
				trainSeatRepository.save(seat);

				// 2. 讓 Booking 自己處理狀態（DDD）
				booking.onSeatBooked();
				ticketBookingRepository.save(booking);

				// 3. 發送 SeatBookedEvent（由 Seat Aggregate）
				this.saveDomainEventsToOutbox(seat.getDomainEvents());

			} else {
				// TODO 其他支付方式
			}

		} catch (Exception e) {

			// 補償事件（Saga 關鍵）
			SeatBookedFailedEvent failEvent = SeatBookedFailedEvent.builder().targetId(command.getBookingUuid())
					.eventTxId(eventTxId).reason(e.getMessage()).build();
			log.warn("建立補償事件: {}", failEvent);
			self.forceSaveDomainEvent(failEvent);
			throw e;
		}
	}

	/**
	 * 進行座位 Check in 動作
	 * 
	 * @param command {@link CheckInTicketCommand}
	 */
	@Transactional
	@Retryable(retryFor = { CannotAcquireLockException.class }, maxAttempts = 3, backoff = @Backoff(delay = 100))
	public void checkInSeat(CheckInSeatCommand command) {
		TrainSeat trainSeat = trainSeatRepository.findByBookUuidAndTakeDateAndSeatNoAndCarNo(command.getUuid(),
				command.getTakeDate(), command.getSeatNo(), command.getCarNo());
		trainSeat.checkIn();
		trainSeatRepository.save(trainSeat);
	}

	/**
	 * 取消劃位(手動)
	 * 
	 * @param command {@link CheckInTicketCommand}
	 */
	@Transactional
	@Retryable(retryFor = { CannotAcquireLockException.class }, maxAttempts = 3, backoff = @Backoff(delay = 100))
	public void cancelSeat(CancelSeatByUserCommand command) {

		ticketBookingRepository.findById(command.getUuid()).ifPresentOrElse(booking -> {
			// 先查該票價
			Ticket ticket = ticketRepository.findByTicketNo(booking.getTicketUuid());

			// 透過 Booking 上的 Account UUID 找到對應的帳戶
			moneyAccountRepository.findById(booking.getAccountUuid()).ifPresentOrElse(account -> {

				TrainSeat trainSeat = trainSeatRepository.findByBookUuidAndTakeDateAndSeatNoAndCarNo(command.getUuid(),
						command.getTakeDate(), command.getSeatNo(), command.getCarNo());
				trainSeat.cancel(ticket.getPrice(), account.getUuid()); // 取消座位
				trainSeatRepository.save(trainSeat);

				// 處理 Domain Event
				List<DomainEvent> domainEvents = account.getDomainEvents();

				// 註冊 Domain Event 到 Outbox
				this.saveDomainEventsToOutbox(domainEvents);

			}, () -> log.error("查無該筆帳戶資訊: Account uuid:{}", booking.getAccountUuid()));

		}, () -> log.error("查無該筆 Ticket Booking 紀錄，Booking uuid:{}", command.getUuid()));

	}

	/**
	 * 取消劃位( SAGA 補償)
	 * 
	 * @param command   {@link CancelSeatCompensationCommand}
	 * @param eventTxId 該業務所屬唯一值
	 */
	@Transactional
	@Retryable(retryFor = { CannotAcquireLockException.class }, maxAttempts = 3, backoff = @Backoff(delay = 100))
	public void cancelSeat(CancelSeatCompensationCommand command, String eventTxId) {

		// 【關鍵】先把 ID 存在區域變數，或是直接用 command 裡的 ID
		String bookingUuid = command.getBookingUuid();

		trainSeatRepository.findByBookUuid(bookingUuid).ifPresentOrElse(seat -> {

			// 執行補償：內部會執行 this.bookUuid = null
			seat.compensateCancel(bookingUuid, eventTxId);

			trainSeatRepository.save(seat);
			this.saveDomainEventsToOutbox(seat.getDomainEvents());

			log.info("座位已釋放，準備關閉訂單: {}", bookingUuid);

		}, () -> log.warn("找不到座位紀錄: {}", bookingUuid));

		// 直接使用剛才存下來的 bookingUuid 變數
		// 不要用 seat.getBookUuid()，因為那已經變 null 了
		bookingCommandService.fail(bookingUuid, eventTxId, "支付失敗，系統自動釋放資源");
	}

	/**
	 * 取消劃位 (通用資源釋放) 職責：僅負責將座位狀態改回可預訂，並發布事件。
	 */
	@Transactional
	@Retryable(retryFor = { CannotAcquireLockException.class }, maxAttempts = 3)
	public void compensateCancel(String bookingUuid, String eventTxId) {
		trainSeatRepository.findByBookUuid(bookingUuid).ifPresentOrElse(seat -> {
			seat.compensateCancel(bookingUuid, eventTxId);
			trainSeatRepository.save(seat);
			this.saveDomainEventsToOutbox(seat.getDomainEvents());
			log.info("SAGA 資源釋放成功 - 座位已解鎖，訂單 UUID: {}", bookingUuid);
		}, () -> log.warn("找不到座位紀錄，忽略釋放動作: {}", bookingUuid));
	}
}
