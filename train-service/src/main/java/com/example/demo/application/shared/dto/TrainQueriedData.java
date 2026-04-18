package com.example.demo.domain.share;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainQueriedData {

	private String uuid;

	private Integer number; // 火車號次

	private String kind; // 火車種類

	private List<StopQueriedData> stops; // 停靠站

}
