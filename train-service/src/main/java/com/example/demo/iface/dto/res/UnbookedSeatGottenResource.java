package com.example.demo.iface.dto.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnbookedSeatGottenResource {

	private String trainUuid;
	
	private Long carNo;
	
	private String seatNo;
}
