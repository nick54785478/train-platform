package com.example.demo.domain.shared.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimetableGeneratedView {

	private TemplateQueriedView templateQueriedData;

	private Map<String, Object> parameters;

	private List<TimetableDetailGeneratedView> details;
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public class TimetableDetailGeneratedView {

		private String uuid; // 火車 UUID

		private String ticketUuid; // 車票 UUID

		private String trainNo; // 火車號次

		private String fromStop; // 起站

		private String toStop; // 迄站

		private String fromStopTime; // 起站停靠時間

		private String toStopTime; // 迄站停靠時間

		private String kind; // 火車種類

	}


}
