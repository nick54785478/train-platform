package com.example.demo.domain.seat.command;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Getter;

@Getter 
@Builder
public class BookSeatCommand {
	
	private String ticketUuid;

	private String trainUuid;

	private String bookingUuid;

	private LocalDate takeDate;

	private String seatNo;

	private Long carNo;
}
