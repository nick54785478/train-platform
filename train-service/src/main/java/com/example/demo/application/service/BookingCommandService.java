package com.example.demo.application.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.application.shared.dto.BookingCheckedInData;
import com.example.demo.application.shared.dto.BookingCompletedData;
import com.example.demo.application.shared.dto.TicketBookedData;
import com.example.demo.base.application.service.BaseApplicationService;
import com.example.demo.base.domain.aggregate.DomainEvent;
import com.example.demo.base.infra.context.ContextHolder;
import com.example.demo.base.shared.enums.YesNo;
import com.example.demo.base.shared.exception.exception.ValidationException;
import com.example.demo.domain.account.aggregate.MoneyAccount;
import com.example.demo.domain.booking.aggregate.TicketBooking;
import com.example.demo.domain.booking.command.BookTicketCommand;
import com.example.demo.domain.booking.command.CancelTicketBookingCommand;
import com.example.demo.domain.booking.command.CheckInTicketBookingCommand;
import com.example.demo.domain.booking.command.CompleteBookingCommand;
import com.example.demo.domain.seat.aggregate.TrainSeat;
import com.example.demo.domain.service.TicketBookingService;
import com.example.demo.infra.repository.MoneyAccountRepository;
import com.example.demo.infra.repository.TicketBookingRepository;
import com.example.demo.infra.repository.TrainSeatRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, timeout = 36000, rollbackFor = Exception.class)
public class BookingCommandService extends BaseApplicationService {

	private final TrainSeatRepository trainSeatRepository;
	private final TicketBookingService ticketBookingService;
	private final MoneyAccountRepository moneyAccountRepository;
	private final TicketBookingRepository ticketBookingRepository;

	/**
	 * 劃位訂票
	 * 
	 * @param command {@link BookTicketCommand}
	 * @return TicketBookedData
	 */
	public TicketBookedData bookTicket(BookTicketCommand command) {

		// 取得當前使用者資訊
		String username = ContextHolder.getUsername();
		String email = ContextHolder.getUserEmail();

		TrainSeat seat = trainSeatRepository.findByTakeDateAndSeatNoAndTrainUuidAndBooked(command.getTakeDate(),
				command.getSeatNo(), command.getTrainUuid(), YesNo.Y);
		// 不能重複劃位
		if (!Objects.isNull(seat)) {
			throw new ValidationException("VALIDATE_FAILED", "該位子已被預定，劃位失敗");
		}

		// 查詢 儲值帳號 資訊
		MoneyAccount account = moneyAccountRepository.findByUsername(username);

		// 呼叫 Domain Service 進行訂位
		TicketBooking ticketBooking = ticketBookingService.book(command, account, username, email);
		ticketBookingRepository.saveAndFlush(ticketBooking); // 這邊要 Save & Flush，否則 Event 可能會在 Commit 前觸發

		// 發布 Domain Event 進行扣款及訂位
		this.saveDomainEventsToOutbox(ticketBooking.getDomainEvents());
		return new TicketBookedData(ticketBooking.getUuid());
	}

	/**
	 * Check in Ticket Booking
	 * 
	 * @param command             {@link CheckInTicketBookingCommand}
	 * @param TicketCheckedInData
	 */
	public BookingCheckedInData checkInBooking(CheckInTicketBookingCommand command) {
		TicketBooking booking = ticketBookingRepository.findById(command.getUuid())
				.orElseThrow(() -> new ValidationException("VALIDATION_EXCEPTION", "發生錯誤，查無此訂位"));

		// 進行 Ticket 的 Check-in 動作
		ticketBookingService.checkInTicket(command, booking);
		TicketBooking saved = ticketBookingRepository.save(booking);

		// 取出 Domain Events
		List<DomainEvent> domainEvents = booking.getDomainEvents();

		// 註冊 Event 到 Outbox
		this.saveDomainEventsToOutbox(domainEvents);

		// 清除 Domain Events
		booking.clearDomainEvents();

		return new BookingCheckedInData(saved.getUuid(), saved.getLastUpdatedDate(), "Check in Successfully!");
	}

	/**
	 * Cancel Ticket Booking
	 * 
	 * @param command             {@link CancelTicketBookingCommand}
	 * @param TicketCancelledData
	 */
	@Transactional
	public void cancelBooking(CancelTicketBookingCommand command, String eventTxId) {
		TicketBooking booking = ticketBookingRepository.findById(command.getUuid())
				.orElseThrow(() -> new EntityNotFoundException("找不到訂單"));

		// 1. 領域邏輯檢查 (例如：已搭乘的票不能取消)
		booking.cancel(eventTxId); // 內部狀態改為 CANCELING

		// 2. 持久化並寫入 Outbox
		ticketBookingRepository.save(booking);
		this.saveDomainEventsToOutbox(booking.getDomainEvents());
	}

	/**
	 * 完成訂位
	 * 
	 * @param command {@link CompleteBookingCommand}
	 */
	public BookingCompletedData completeBooking(CompleteBookingCommand command) {

		TicketBooking booking = ticketBookingRepository.findById(command.getBookingUuid()).orElse(null);

		if (booking == null) {
			return null;
		}

		booking.complete();
		ticketBookingRepository.save(booking);
		// 處理 Domain Events
		List<DomainEvent> domainEvents = booking.getDomainEvents();
		// 註冊 Event 到 Outbox
		this.saveDomainEventsToOutbox(domainEvents);
		return new BookingCompletedData(booking.getEmail());
	}

	/**
	 * 標註 SAGA 狀態為 Fail
	 * 
	 * @param bookingUuid 訂單唯一值
	 * @param eventTxId   該筆事件業務唯一值
	 * @param reason      失敗原因
	 */
	public void fail(String bookingUuid, String eventTxId, String reason) {
		ticketBookingRepository.findById(bookingUuid).ifPresent(booking -> {

			// 取得使用者資訊(信箱)，用於後續通知
			moneyAccountRepository.findById(booking.getAccountUuid()).ifPresent(e -> {
				booking.fail(eventTxId, reason, e.getEmail());
				ticketBookingRepository.save(booking);
				// 處理 Domain Events
				List<DomainEvent> domainEvents = booking.getDomainEvents();
				// 註冊 Event 到 Outbox
				this.saveDomainEventsToOutbox(domainEvents);
			});

		});
	}

}
