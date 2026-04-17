package com.example.demo.domain.train.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStopCommand {
	
	private String uuid;

	private Integer seq;  // 停站順序
	
	private String stopName;	// 站名
	
	private String stopTime;	// 停靠時間
	
	private String deleteFlag = "N";
}
