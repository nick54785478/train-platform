package com.example.demo.domain.booking.command;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelTicketBookingCommand {
	
	private String uuid; // booking uuid

	private String seatNo; // 座號

	private Long carNo; // 車廂編號

	private LocalDate takeDate; // 搭乘日期

}
