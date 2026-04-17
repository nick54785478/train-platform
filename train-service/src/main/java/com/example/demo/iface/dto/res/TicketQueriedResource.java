package com.example.demo.iface.dto.res;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketQueriedResource {

	private String ticketNo; 
	
	private String fromStop;
	
	private String toStop;

	private BigDecimal price;
}
