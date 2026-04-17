package com.example.demo.domain.ticket.command;

import java.math.BigDecimal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Valid
@AllArgsConstructor
@NoArgsConstructor
public class CreateTicketCommand {

	private Integer trainNo;

	@NotBlank
	private String fromStop;

	@NotBlank
	private String toStop;
	
	private BigDecimal price;  // 票價
}
