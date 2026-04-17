package com.example.demo.iface.dto.res;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingCheckedInResource {

	private String uuid;
	
	private Date chekedInDate;
	
	private String message;
}
