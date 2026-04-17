package com.example.demo.infra.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.base.shared.enums.YesNo;
import com.example.demo.domain.seat.aggregate.TrainSeat;

@Repository
public interface TrainSeatRepository extends JpaRepository<TrainSeat, Long> {

	List<TrainSeat> findByTrainUuidAndTakeDateAndActiveFlag(String trainUuid, LocalDate takeDate, YesNo activeFlag);

	List<TrainSeat> findByActiveFlagAndTakeDateBefore(YesNo activeFlag, LocalDate takeDate);

	TrainSeat findByTrainUuidAndSeatNoAndTakeDateAndActiveFlag(String trainUuid, String seatNo, LocalDate takeDate,
			YesNo activeFlag);

	TrainSeat findByBookUuidAndSeatNoAndTakeDateAndActiveFlag(String uuid, String seatNo, LocalDate takeDate,
			YesNo activeFlag);

	List<TrainSeat> findBySeatNoAndTakeDate(String seatNo, LocalDate takeDate);

	TrainSeat findByBookUuidAndTakeDateAndSeatNoAndCarNo(String bookUuid, LocalDate takeDate, String seatNo,
			Long carNo);

	TrainSeat findByTakeDateAndSeatNoAndTrainUuidAndBooked(LocalDate takeDate, String seatNo, String trainUuid,
			YesNo activeFlag);

	List<TrainSeat> findByTakeDateAndTrainUuidAndBookedAndActiveFlag(LocalDate takeDate, String trainUuid, YesNo booked,
			YesNo activeFlag);

	Optional<TrainSeat> findByBookUuidAndSeatNoAndTakeDateAndAndBookedAndActiveFlag(String uuid, String seatNo,
			LocalDate takeDate, YesNo booked, YesNo activeFlag);

	List<TrainSeat> findByBookUuidIn(List<String> bookingList);

	Optional<TrainSeat> findByBookUuid(String bookingUuid);
}
