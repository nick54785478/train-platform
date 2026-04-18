package com.example.demo.domain.shared.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketQueriedView {

	private String ticketNo;

	private String fromStop;

	private String toStop;

	private BigDecimal price;
}
