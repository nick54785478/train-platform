package com.example.demo.domain.share.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnbookedSeatGottenView {

	private String trainUuid;
	
	private Long carNo;
	
	private String seatNo;
}
