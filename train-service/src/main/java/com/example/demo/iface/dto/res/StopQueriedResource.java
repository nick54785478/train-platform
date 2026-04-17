package com.example.demo.iface.dto.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StopQueriedResource {
	
	private String uuid;
	
	private Integer seq;

	private String stopName;
	
	private String stopTime;
	
	private String deleteFlag;
}
