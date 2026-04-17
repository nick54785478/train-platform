package com.example.demo.domain.share;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimetableDetailGeneratedData {

	private String uuid; // 火車 UUID

	private String ticketUuid; // 車票 UUID

	private String trainNo; // 火車號次

	private String fromStop; // 起站

	private String toStop; // 迄站

	private String fromStopTime; // 起站停靠時間

	private String toStopTime; // 迄站停靠時間

	private String kind; // 火車種類

}
