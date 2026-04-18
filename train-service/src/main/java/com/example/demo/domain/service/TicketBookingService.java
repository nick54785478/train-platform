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
import com.example.demo.domain.share.dto.TrainSeatBookedView;
import com.example.demo.domain.ticket.aggregate.Ticket;
import com.example.demo.domain.train.aggregate.Train;
import com.example.demo.domain.train.aggregate.entity.TrainStop;
import com.example.demo.domain.train.aggregate.vo.TrainKind;
import com.example.demo.infra.repository.TicketBookingRepository;
import com.example.demo.infra.repository.TicketRepository;
import com.example.demo.infra.repository.TrainRepository;
import com.example.demo.infra.repository.TrainSeatRepository;

import jakarta.transaction.Transactional;
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
	 * 查詢個人訂票資訊
	 * 
	 * @param username 使用者名稱
	 * @return BookingQueriedData
	 */
	@Transactional
	public BookingQueriedView queryBooking(String username) {
		List<TicketBooking> bookingList = ticketBookingRepository.findByUsername(username);
		List<String> bookingUuidList = bookingList.stream().map(TicketBooking::getUuid).collect(Collectors.toList());

		// 座位資料
		List<TrainSeat> trainSeatList = trainSeatRepository.findByBookUuidIn(bookingUuidList);
		// 座位 Map <bookUuid, Entity>
		Map<String, List<TrainSeat>> seatMap = trainSeatList.stream()
				.collect(Collectors.groupingBy(TrainSeat::getBookUuid));

		// 透過火車座位查出對應火車的 uuid 清單
		List<String> trainUuidList = trainSeatList.stream().map(TrainSeat::getTrainUuid).collect(Collectors.toList());
		// 透過 火車 uuid 清單查出火車資訊
		List<Train> trainList = trainRepository.findByUuidIn(trainUuidList);
		// 火車 Map<火車 Uuid, Train>
		Map<String, Train> trainMap = trainList.stream().collect(Collectors.toMap(Train::getUuid, Function.identity()));

		// 透過火車座位查出對應車票的 uuid 清單
		List<String> ticketUuidList = trainSeatList.stream().map(TrainSeat::getTicketUuid).collect(Collectors.toList());
		// 車票 Map<車票 uuid, Train>
		Map<String, Ticket> ticketMap = ticketReposiotry.findByTicketNoIn(ticketUuidList).stream()
				.collect(Collectors.toMap(Ticket::getTicketNo, Function.identity()));

		BookingQueriedView bookingQueriedData = new BookingQueriedView();
		bookingQueriedData.setUsername(username);

		List<TrainSeatBookedView> bookedDatas = new ArrayList<>();

		bookingList.stream().forEach(book -> {
			TrainSeatBookedView bookedData = new TrainSeatBookedView();
			this.setTrainSeatBookedData(bookedData, book, ticketMap, trainMap, seatMap);
			bookedDatas.add(bookedData);
		});

		bookingQueriedData.setBookedDatas(bookedDatas);
		return bookingQueriedData;
	}

	/**
	 * 設置 Train Seat Booking 資料
	 * 
	 * @param bookedData 被設置的 Booking 資料
	 * @param book       Booking 領域資料
	 * @param ticketMap  車票 Map<車票 Uuid, Train> 資料
	 * @param trainMap   火車 Map<火車Uuid, Train> 資料
	 * @param seatMap    座位 Map<bookUuid, Entity> 資料
	 */
	private void setTrainSeatBookedData(TrainSeatBookedView bookedData, TicketBooking book,
			Map<String, Ticket> ticketMap, Map<String, Train> trainMap, Map<String, List<TrainSeat>> seatMap) {
		if (!Objects.isNull(ticketMap.get(book.getTicketUuid()))) {
			var ticketData = ticketMap.get(book.getTicketUuid());
			bookedData.setFrom(ticketData.getFromStop()); // 起站
			bookedData.setTo(ticketData.getToStop()); // 迄站
		}

		if (!Objects.isNull(trainMap.get(book.getTrainUuid()))) {
			var trainData = trainMap.get(book.getTrainUuid());
			bookedData.setNumber(trainData.getNumber()); // 火車車次
			bookedData.setKind(TrainKind.fromLabel(trainData.getKind().getLabel()).getLabel()); // 火車名稱

			// Map <站名, 發車時間>
			Map<String, LocalTime> stopMap = trainData.getStops().stream()
					.collect(Collectors.toMap(TrainStop::getName, TrainStop::getTime));
			// 設置發車時間
			bookedData.setStartTime(
					!Objects.isNull(stopMap.get(bookedData.getFrom())) ? stopMap.get(bookedData.getFrom()) : null);
			// 設置抵達時間
			bookedData.setArriveTime(
					!Objects.isNull(stopMap.get(bookedData.getTo())) ? stopMap.get(bookedData.getTo()) : null);
			log.debug("起站:{}, 迄站:{}, 發車時間:{}, 抵達時間:{}", bookedData.getFrom(), bookedData.getTo(),
					bookedData.getStartTime(), bookedData.getArriveTime());
		}

		if (!Objects.isNull(seatMap.get(book.getUuid()))) {
			var seatList = seatMap.get(book.getUuid());
			Map<String, TrainSeat> collectMap = seatList.stream()
					.collect(Collectors.toMap(TrainSeat::getBookUuid, Function.identity()));

			if (!Objects.isNull(collectMap.get(book.getUuid()))) {
				var trainSeat = collectMap.get(book.getUuid());
				bookedData.setSeatNo(trainSeat.getSeatNo());
				bookedData.setTakeDate(trainSeat.getTakeDate());
				bookedData.setActiveFlag(trainSeat.getActiveFlag());
				bookedData.setBooked(trainSeat.getBooked());
				bookedData.setCarNo(trainSeat.getCarNo());
			}
		}
	}

}
