package com.example.demo.iface.dto.res;

import java.time.LocalDate;
import java.time.LocalTime;

import com.example.demo.base.shared.enums.YesNo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookQueriedResource {

	private Integer number; // 車次

	private String kind; // 火車種類

	private String from; // 起站

	private LocalTime startTime; // 發車時間

	private String to; // 迄站

	private LocalTime arriveTime; // 抵達時間

	private LocalDate takeDate; // 乘車日期
	
	private String carNo; // 車廂編號

	private String seatNo; // 座號

	private YesNo booked; // 是否已預定

	private YesNo activeFlag; // 是否已失效

}
