package com.example.demo.domain.share.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainQueriedView {

	private String uuid;

	private Integer number; // 火車號次

	private String kind; // 火車種類

	private List<StopQueriedView> stops; // 停靠站

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class StopQueriedView {

		private String uuid;

		private Integer seq; // 停站順序

		private String name; // 站名

		private String time; // 停站時間

		private String deleteFlag;
	}
}
