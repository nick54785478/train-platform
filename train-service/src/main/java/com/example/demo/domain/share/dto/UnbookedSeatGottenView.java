package com.example.demo.domain.share;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnbookedSeatGottenData {

	private String trainUuid;
	
	private Long carNo;
	
	private String seatNo;
}
