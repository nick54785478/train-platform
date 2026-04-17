package com.example.demo.domain.seat.aggregate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.example.demo.base.domain.aggregate.BaseAggreagteRoot;
import com.example.demo.base.shared.enums.YesNo;
import com.example.demo.domain.seat.command.BookSeatCommand;
import com.example.demo.domain.seat.outbound.SeatBookedEvent;
import com.example.demo.domain.seat.outbound.SeatCancelledByUserEvent;
import com.example.demo.domain.seat.outbound.SeatCancelledEvent;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "TRAIN_SEAT")
public class TrainSeat extends BaseAggreagteRoot {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "TICKET_UUID")
	private String ticketUuid; // Ticket 對應的 UUID

	@Column(name = "TRAIN_UUID")
	private String trainUuid; // 火車對應的 UUID

	@Column(name = "BOOK_UUID")
	private String bookUuid; // 預訂座位的 UUID

	@Column(name = "TAKE_DATE")
	private LocalDate takeDate; // 乘車日期

	@Column(name = "SEAT_NO")
	private String seatNo; // 座號

	@Column(name = "CAR_NO")
	private Long carNo; // 車廂編號

	@Column(name = "BOOKED")
	@Enumerated(EnumType.STRING)
	private YesNo booked; // 是否已預定

	@Column(name = "ACTIVE_FLAG")
	@Enumerated(EnumType.STRING)
	private YesNo activeFlag; // 是否已失效

	/**
	 * book seat
	 * 
	 * @param command     {@link BookSeatCommand}
	 * @param eventTxId   該業務所屬唯一值: uuid
	 * @param accountUuid 帳號所屬唯一值: uuid
	 * @param amount      金額
	 * @return TrainSeat
	 */
	public static TrainSeat create(BookSeatCommand command, String eventTxId, String accountUuid, BigDecimal amount) {
		TrainSeat seat = new TrainSeat();
		seat.ticketUuid = command.getTicketUuid();
		seat.trainUuid = command.getTrainUuid();
		seat.bookUuid = command.getBookingUuid();
		seat.takeDate = command.getTakeDate();
		seat.carNo = command.getCarNo();
		seat.seatNo = command.getSeatNo();
		seat.booked = YesNo.Y;
		seat.activeFlag = YesNo.Y;

		// 發布 Domain Event : Seat Booked Event
		// 用途 : 用來付款
		SeatBookedEvent event = SeatBookedEvent.builder().bookingUuid(command.getBookingUuid()).targetId(accountUuid)
				.money(amount).build();
		// 賦予 Event Transaction Id
		seat.assignEventTxId(eventTxId);
		// 設置 Domain Event
		seat.raiseEvent(event);
		return seat;
	}

	/**
	 * check in 座位
	 */
	public void checkIn() {
		this.activeFlag = YesNo.N;
	}

	/**
	 * 取消座位
	 */
	public void cancel(BigDecimal refund, String accountUuid) {
		this.activeFlag = YesNo.N;
		this.booked = YesNo.N;

		// 建立退款事件
		SeatCancelledByUserEvent event = SeatCancelledByUserEvent.builder().refund(refund)
				.eventLogUuid(UUID.randomUUID().toString()).targetId(accountUuid).build();

		// 設置 Domain Event
		this.raiseEvent(event);
	}

	/**
	 * 取消座位 (SAGA 補償措施)
	 * 
	 * @param bookingUuid Booking 所屬唯一值: uuid
	 * @param eventTxId   該業務所屬唯一值: uuid
	 */
	public void compensateCancel(String bookingUuid, String eventTxId) {

		// 1. 冪等檢查：如果已經是 N，代表補償已完成，直接 return
		if (this.booked == YesNo.N && this.activeFlag == YesNo.N) {
			log.info("Seat {} already cancelled for booking {}", this.id, bookingUuid);
			return;
		}

		// 2. 安全檢查：確保這個座位真的是屬於該訂單 (防止誤刪)
		if (this.bookUuid != null && !this.bookUuid.equals(bookingUuid)) {
			log.error("Conflict: Seat {} belongs to {} but compensation requested for {}", this.id, this.bookUuid,
					bookingUuid);
			return; // 或者拋出異常，視業務嚴謹度而定
		}

		// 3. 狀態變更
		this.activeFlag = YesNo.N;
		this.booked = YesNo.N;

		// 關鍵：解除與訂單的關聯，讓該位置能被重新預訂
		this.bookUuid = null;
		this.assignEventTxId(eventTxId);

		// 4. 發布事件
		SeatCancelledEvent event = SeatCancelledEvent.builder().bookingUuid(bookingUuid).targetId(bookingUuid)
				.eventTxId(eventTxId).build();
		this.raiseEvent(event);

		log.info("Saga Compensation: Seat released for booking {}", bookingUuid);
	}

	/**
	 * 使票券失效
	 */
	public void inactive() {
		this.activeFlag = YesNo.N;
	}

}
