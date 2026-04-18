package com.example.demo.domain.share;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StopQueriedData {
	
	private String uuid;

	private Integer seq; // 停站順序
	
	private String name; // 站名

	private String time; // 停站時間
	
	private String deleteFlag;
}
