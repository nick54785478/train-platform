package com.example.demo.domain.service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.demo.base.domain.service.BaseDomainService;
import com.example.demo.base.shared.enums.YesNo;
import com.example.demo.base.shared.exception.exception.ValidationException;
import com.example.demo.domain.account.aggregate.MoneyAccount;
import com.example.demo.domain.booking.aggregate.TicketBooking;
import com.example.demo.domain.booking.command.BookTicketCommand;
import com.example.demo.domain.booking.command.CheckInTicketBookingCommand;
import com.example.demo.domain.seat.aggregate.TrainSeat;
import com.example.demo.domain.share.dto.BookingQueriedView;
import com.example.demo.domain.share.dto.BookingQueriedView.BookingDetailQueriedView;
import com.example.demo.domain.share.dto.TrainSeatBookedView;
import com.example.demo.domain.ticket.aggregate.Ticket;
import com.example.demo.domain.train.aggregate.Train;
import com.example.demo.domain.train.aggregate.entity.TrainStop;
import com.example.demo.domain.train.aggregate.vo.TrainKind;
import com.example.demo.infra.repository.TicketBookingRepository;
import com.example.demo.infra.repository.TicketRepository;
import com.example.demo.infra.repository.TrainRepository;
import com.example.demo.infra.repository.TrainSeatRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Domain Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TicketBookingService extends BaseDomainService {

	private final TicketBookingRepository ticketBookingRepository;
	private final TrainSeatRepository trainSeatRepository;
	private final TrainRepository trainRepository;
	private final TicketRepository ticketReposiotry;

	/**
	 * 車票訂位
	 * 
	 * @param command  訂位命令
	 * @param account  訂票帳號
	 * @param username 帳號
	 * @param email    信箱
	 * @return 成功訊息
	 */
	public TicketBooking book(BookTicketCommand command, MoneyAccount account, String username, String email) {

		// 建立 TicketBooking（會產生 event）
		TicketBooking ticketBooking = TicketBooking.create(command, account, username, email);
		log.info("ticketBooking: {}", ticketBooking);

		// 儲存 Ticket Booking 資訊
		return ticketBooking;
	}

	/**
	 * 進行 Ticket 的 Check-in 動作
	 * 
	 * @param command {@link CheckInTicketBookingCommand}
	 * @param booking Booking 資料
	 */
	public void checkInTicket(CheckInTicketBookingCommand command, TicketBooking booking) {
		// 進行領域檢核 -> 確認該座位尚未 check in
		TrainSeat trainSeat = trainSeatRepository
				.findByBookUuidAndSeatNoAndTakeDateAndAndBookedAndActiveFlag(command.getUuid(), command.getSeatNo(),
						command.getTakeDate(), YesNo.Y, YesNo.Y)
				.orElseThrow(() -> new ValidationException("VALIDATE_FAILED", "該座位資料已失效，Check in 失敗"));

		// 進行 Check in 動作
		booking.checkIn(trainSeat.getTakeDate(), trainSeat.getSeatNo(), trainSeat.getCarNo());
	}

//	/**
//	 * 退費取消訂票
//	 * 
//	 */
//	public TicketRefundedData refund(RefundTicketCommand command) {
//		Optional<TicketBooking> option = ticketBookingRepository.findById(command.getUuid());
//		if (option.isPresent()) {
//			TicketBooking booking = option.get();
//			booking.refund(command);
//			TicketBooking saved = ticketBookingRepository.save(booking);
//
//			BaseEvent event = ContextHolder.getEvent();
//			this.generateEventLog(bookingQueueName, event.getEventLogUuid(), event.getTargetId(), event);
//			return new TicketRefundedData(saved.getTrainUuid(), saved.getCreatedDate(), "Refunded Successfully");
//		}
//		log.error("發生錯誤，查無此預約");
//		throw new ValidationException("VALIDATION_EXCEPTION", "發生錯誤，查無此預約");
//
//	}

	/**
	 * 查詢個人訂票資訊 (修正版)
	 */
	public BookingQueriedView queryBooking(String username) {
		List<TicketBooking> bookingList = ticketBookingRepository.findByUsername(username);
		if (bookingList.isEmpty()) {
			return new BookingQueriedView(username, new ArrayList<>());
		}

		List<String> bookingUuidList = bookingList.stream().map(TicketBooking::getUuid).collect(Collectors.toList());

		// 1. 直接從 bookingList 提取 Train 和 Ticket 的 ID (不再透過 Seat)
		List<String> trainUuidList = bookingList.stream()
				.map(TicketBooking::getTrainUuid)
				.filter(Objects::nonNull)
				.distinct()
				.collect(Collectors.toList());
		
		List<String> ticketUuidList = bookingList.stream()
				.map(TicketBooking::getTicketUuid)
				.filter(Objects::nonNull)
				.distinct()
				.collect(Collectors.toList());

		// 2. 獲取資料 Map
		Map<String, Train> trainMap = trainRepository.findByUuidIn(trainUuidList).stream()
				.collect(Collectors.toMap(Train::getUuid, Function.identity()));

		Map<String, Ticket> ticketMap = ticketReposiotry.findByTicketNoIn(ticketUuidList).stream()
				.collect(Collectors.toMap(Ticket::getTicketNo, Function.identity()));

		// 3. 獲取座位資料 (已取消的訂單在此 Map 中會找不到資料，這是正確的)
		List<TrainSeat> trainSeatList = trainSeatRepository.findByBookUuidIn(bookingUuidList);
		Map<String, List<TrainSeat>> seatMap = trainSeatList.stream()
				.collect(Collectors.groupingBy(TrainSeat::getBookUuid));

		
		BookingQueriedView bookingQueriedData = new BookingQueriedView();
		bookingQueriedData.setUsername(username);

		List<BookingDetailQueriedView> bookedDatas = new ArrayList<>();

		// 4. 組裝資料
		bookingList.forEach(book -> {
			BookingDetailQueriedView bookedData = new BookingDetailQueriedView();
			// 即使沒有座位，基本的車次與訂單狀態也要顯示出來
			this.setTrainSeatBookedData(bookedData, book, ticketMap, trainMap, seatMap);
			bookedDatas.add(bookedData);
		});

		bookingQueriedData.setBookedDatas(bookedDatas);
		return bookingQueriedData;
	}

	/**
	 * 設置 Train Seat Booking 資料 (強化補空邏輯)
	 */
	private void setTrainSeatBookedData(BookingDetailQueriedView bookedData, TicketBooking book,
			Map<String, Ticket> ticketMap, Map<String, Train> trainMap, Map<String, List<TrainSeat>> seatMap) {
		
		// A. 設置基礎訂單狀態 (永遠會有值)
		bookedData.setActiveFlag(book.getActiveFlag());
		bookedData.setStatus(book.getStatus().name()); // 建議 DTO 加上這個，前端才知道是 CANCELLED

		// B. 設置車票資訊 (直接從 book.getTicketUuid() 找)
		Ticket ticketData = ticketMap.get(book.getTicketUuid());
		if (ticketData != null) {
			bookedData.setFrom(ticketData.getFromStop());
			bookedData.setTo(ticketData.getToStop());
		}

		// C. 設置火車資訊 (直接從 book.getTrainUuid() 找)
		Train trainData = trainMap.get(book.getTrainUuid());
		if (trainData != null) {
			bookedData.setNumber(trainData.getNumber());
			bookedData.setKind(trainData.getKind().getLabel());

			Map<String, LocalTime> stopMap = trainData.getStops().stream()
					.collect(Collectors.toMap(TrainStop::getName, TrainStop::getTime));
			
			if (bookedData.getFrom() != null) {
				bookedData.setStartTime(stopMap.get(bookedData.getFrom()));
			}
			if (bookedData.getTo() != null) {
				bookedData.setArriveTime(stopMap.get(bookedData.getTo()));
			}
		}

		// D. 處理座位資訊 (已取消的訂單會進不來這裡，所以要維持原本的 null 或給預設值)
		List<TrainSeat> seatList = seatMap.get(book.getUuid());
		if (seatList != null && !seatList.isEmpty()) {
			TrainSeat trainSeat = seatList.get(0); // 假設一個訂單對一個位子
			bookedData.setSeatNo(trainSeat.getSeatNo());
			bookedData.setTakeDate(trainSeat.getTakeDate());
			bookedData.setBooked(trainSeat.getBooked());
			bookedData.setCarNo(trainSeat.getCarNo());
		} else {
			// 如果沒有座位資料，給予明確的提示或保持 null
			bookedData.setSeatNo("無 (已釋放/取消)");
			bookedData.setBooked(YesNo.N);
		}
	}

}
