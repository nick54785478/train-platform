package com.example.demo.application.shared.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingQueriedData {

	private String username; // 使用者帳號

	private List<TrainSeatBookedData> bookedDatas;

}
