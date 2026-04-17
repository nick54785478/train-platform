package com.example.demo.iface.dto.res;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainQueriedResource {

	private String uuid;

	private Integer number; // 火車號次

	private String kind; // 火車種類

	private List<StopQueriedResource> stops; // 停靠站

}
