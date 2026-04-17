package com.example.demo.domain.share;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatQueriedData {
	
	private Long id;

	private String trainUuid; // 火車對應的 UUID

	private String bookUuid; // 預訂座位的 UUID

	private LocalDate takeDate; // 乘車日期

	private String seatNo; // 座號

}
