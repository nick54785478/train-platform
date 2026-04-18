package com.example.demo.domain.share.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StopDetailQueriedView {

	private Integer seq; // 迄站停靠順序

	private String fromStop; // 起站
	
	private String toStop; // 迄站

	private String arriveStartStopTime; // 起站發車時間
	
	private String arriveEndStopTime; // 抵達迄站時間
	
	private BigDecimal price; // 票價
	
}
