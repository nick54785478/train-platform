package com.example.demo.iface.dto.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketBookedResource {

	private String code;
	
	private String message;
	
	private String uuid;
}
