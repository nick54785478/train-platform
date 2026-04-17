package com.example.demo.iface.dto.res;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainSummaryQueriedResource {

	private String uuid; // 火車 UUID

	private Integer trainNo; // 火車號次

	private String fromStop; // 起站

	private String toStop; // 迄站

	private String fromStopTime; // 起站停靠時間

	private String toStopTime; // 迄站停靠時間

	private String kind; // 火車種類

	private List<StopSummaryQueriedResource> stops = new ArrayList<>(); // 停靠站
}
