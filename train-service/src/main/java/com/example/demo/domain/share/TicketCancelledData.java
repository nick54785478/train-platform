package com.example.demo.domain.share;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketCancelledData {

	private String uuid;
	
	private Date chekedInDate;
	
	private String message;
}
