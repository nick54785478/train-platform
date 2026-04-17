package com.example.demo.domain.account.command;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 收取費用命令
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChargeFareCommand {

	private String uuid;
	
	private String bookingUuid; // TicketBooking UUID

	private BigDecimal money; // 金額
}
