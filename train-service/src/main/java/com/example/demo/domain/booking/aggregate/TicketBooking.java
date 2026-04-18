package com.example.demo.domain.booking.aggregate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

import com.example.demo.base.domain.aggregate.BaseAggreagteRoot;
import com.example.demo.base.shared.enums.YesNo;
import com.example.demo.domain.account.aggregate.MoneyAccount;
import com.example.demo.domain.booking.aggregate.vo.BookingStatus;
import com.example.demo.domain.booking.aggregate.vo.TakeStatus;
import com.example.demo.domain.booking.command.BookTicketCommand;
import com.example.demo.domain.booking.outbound.BookingCancelledEvent;
import com.example.demo.domain.booking.outbound.BookingCheckedInEvent;
import com.example.demo.domain.booking.outbound.BookingCreatedEvent;
import com.example.demo.domain.booking.outbound.BookingExecutedFailedEvent;
import com.example.demo.domain.shared.enums.TicketAction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
@Table(name = "TICKET_BOOKING")
public class TicketBooking extends BaseAggreagteRoot {

	@Id
	@Column(name = "UUID")
	private String uuid;

	@Column(name = "USERNAME")
	private String username; // 使用者帳號

	@Column(name = "EMAIL")
	private String email; // Email

	@Column(name = "TICKET_UUID")
	private String ticketUuid; // 對應車票班次

	@Column(name = "TRAIN_UUID")
	private String trainUuid; // 該車次對應的 UUID

	@Column(name = "ACCOUNT_UUID")
	private String accountUuid; // 帳號 UUID

	@Column(name = "TAKE_STATUS")
	@Enumerated(EnumType.STRING)
	private TakeStatus takeStatus; // 搭乘狀態

	@Column(name = "PRICE")
	private BigDecimal price; // 票價，即當初付了多少錢

	@Column(name = "STATUS")
	@Enumerated(EnumType.STRING)
	private BookingStatus status;

	@Enumerated(EnumType.STRING)
	@Column(name = "ACTIVE_FLAG")
	private YesNo activeFlag; // 是否失效 (過期、取消訂位)

	/**
	 * 建立訂位訂單
	 * 
	 * @param command      預訂車票命令
	 * @param MoneyAccount 訂票帳戶相關資訊
	 * @param username     使用者名稱
	 * @param email        信箱
	 */
	public static TicketBooking create(BookTicketCommand command, MoneyAccount account, String username, String email) {
		TicketBooking ticketBooking = new TicketBooking();
		ticketBooking.uuid = UUID.randomUUID().toString();
		ticketBooking.trainUuid = command.getTrainUuid();
		ticketBooking.ticketUuid = command.getTicketUuid();
		ticketBooking.username = username;
		ticketBooking.email = email;
		ticketBooking.price = command.getPrice();
		ticketBooking.takeStatus = TakeStatus.UNTAKEN;
		ticketBooking.status = BookingStatus.INIT;
		ticketBooking.accountUuid = (Objects.isNull(account)) ? null : account.getUuid();
		ticketBooking.activeFlag = YesNo.Y;

		// 建立一個 Domain Event : Booking Created Event
		// 用途 : Book Seat
		BookingCreatedEvent event = BookingCreatedEvent.builder().targetId(ticketBooking.uuid)
				.takeDate(command.getTakeDate()).method(command.getPayMethod()).seatNo(command.getSeatNo())
				.carNo(command.getCarNo()).price(command.getPrice()).build();

		// 設置 Domain Event
		ticketBooking.raiseEvent(event);
		return ticketBooking;
	}

	/**
	 * 進行 Check in 動作
	 * 
	 * @param takeDate 搭乘日期
	 * @param seatNo   乘坐位置
	 * @param carNo    車廂編號
	 */
	public void checkIn(LocalDate takeDate, String seatNo, Long carNo) {
		this.takeStatus = TakeStatus.TAKEN;
		this.activeFlag = YesNo.N;

		// 建立 Domain Event : 車票 Check in 事件
		BookingCheckedInEvent event = BookingCheckedInEvent.builder().targetId(this.uuid).takeDate(takeDate)
				.action(TicketAction.CHECK_IN.getName()).seatNo(seatNo).carNo(carNo).build();
		// 設置 Domain Event
		this.raiseEvent(event);
	}

	/**
	 * 執行取消訂單邏輯
	 */
	public void cancel(String sagaEventTxId) {
		// 1. 狀態檢查：已取消或已搭乘則不處理
		if (this.status == BookingStatus.CANCELLED || this.takeStatus == TakeStatus.TAKEN) {
			log.warn("訂單 {} 狀態為 {}，無法執行取消", this.uuid, this.status);
			return;
		}

		// 2. 變更狀態
		this.status = BookingStatus.CANCELLED;
		this.activeFlag = YesNo.N; // 標記為失效

		// 3. 同步 Saga ID 防止跳號
		this.assignEventTxId(sagaEventTxId);

		// 4. 註冊事件 (將原本的 seatId 改為傳遞 bookingUuid)
		BookingCancelledEvent event = BookingCancelledEvent.builder().targetId(this.accountUuid) // 錢退回給誰
				.eventTxId(sagaEventTxId).bookingUuid(this.uuid) // 讓 Seat 服務用此 ID 去解鎖座位
				.refundAmount(this.price) // 退款金額
				.build();

		this.raiseEvent(event);
	}

	/**
	 * Seat 預定成功
	 */
	public void onSeatBooked() {

		if (this.status != BookingStatus.INIT) {
			throw new IllegalStateException("Invalid state transition");
		}

		this.status = BookingStatus.SEAT_BOOKED;
	}

	/**
	 * 扣款成功：防禦性編寫
	 */
	public void onPaid() {
		// 如果已經是 PAID，代表這是重複消費，直接 return 即可 (冪等)
		if (this.status == BookingStatus.PAID) {
			return;
		}

		// 如果狀態不是預期的 SEAT_BOOKED，則需要確認是否為異常
		if (this.status != BookingStatus.SEAT_BOOKED) {
			throw new IllegalStateException("無法從 " + this.status + " 轉換至 PAID");
		}

		this.status = BookingStatus.PAID;
	}

	/**
	 * Saga 完成
	 */
	public void complete() {
		// 冪等處理：如果已經完成，不要報錯
		if (this.status == BookingStatus.COMPLETED) {
			return;
		}

		if (this.status != BookingStatus.PAID) {
			// 這裡 log 出目前的狀態，方便 debug
			throw new IllegalStateException("無法完成 Saga，目前訂單狀態為: " + this.status);
		}

		this.status = BookingStatus.COMPLETED;
	}

	/**
	 * Saga 失敗（統一入口）
	 * 
	 * @param eventTxId 該業務唯一值
	 * @param reason    失敗原因
	 * @param email     被通知者信箱
	 */
	public void fail(String eventTxId, String reason, String email) {
		// 冪等檢查
		if (this.status == BookingStatus.FAILED) {
			return;
		}

		this.status = BookingStatus.FAILED;
		this.activeFlag = YesNo.N;

		// 建立一個終點事件：Saga 失敗
		BookingExecutedFailedEvent event = BookingExecutedFailedEvent.builder().targetId(this.uuid).eventTxId(eventTxId)
				.email(email).reason(reason).build();
		this.raiseEvent(event); // 放入 Outbox
	}
}
