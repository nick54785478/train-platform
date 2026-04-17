package com.example.demo.domain.booking.command;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookTicketCommand {

	private String trainUuid; // 火車 UUID

	private String ticketUuid; // 車票 UUID

	private Integer trainNo; // 火車號次
	
	private BigDecimal price; // 價格
	
	private Long carNo;	// 車廂編號

	private String seatNo; // 座號

	private LocalDate takeDate; // 乘車日期
	
	private String payMethod; // 付款方式
	
}
