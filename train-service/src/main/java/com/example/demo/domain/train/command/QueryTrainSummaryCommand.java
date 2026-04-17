package com.example.demo.domain.train.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class QueryTrainSummaryCommand {

	private Integer trainNo; // 未必會使用單一車次
	
	private String trainKind;

	private String fromStop;

	private String toStop;
	
	private String takeDate;	// 目前日期都固定

	private String time;

}
