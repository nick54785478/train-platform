package com.example.demo.domain.share;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketQueriedData {

	private String ticketNo;
	
	private String fromStop;
	
	private String toStop;

	private BigDecimal price;
}
