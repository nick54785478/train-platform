package com.example.demo.domain.booking.command;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckInTicketBookingCommand {

	private String uuid; // booking uuid

	private String seatNo; // 座號

	private Long carNo; // 車廂編號

	private LocalDate takeDate; // 搭乘日期

}
