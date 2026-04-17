package com.example.demo.domain.seat.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelSeatCompensationCommand {

	private String bookingUuid;
	
	private String accountUuid;
	
	private String reason;
}
