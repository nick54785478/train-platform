package com.example.demo.domain.share;

import java.math.BigDecimal;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainDetailQueriedData {

	private String uuid; // 火車 UUID
	
	private String ticketUuid; // 車票的 uuid

	private Integer trainNo; // 火車號次

	private String fromStop; // 起站

	private String toStop; // 迄站

	private LocalTime fromStopTime; // 起站停靠時間

	private LocalTime toStopTime; // 迄站停靠時間

	private String kind; // 火車種類
	
	private String takeDate;

	private BigDecimal price; // 售票價格

}
