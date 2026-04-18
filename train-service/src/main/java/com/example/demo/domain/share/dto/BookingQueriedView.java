package com.example.demo.domain.share.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingQueriedView {

	private String username; // 使用者帳號

	private List<TrainSeatBookedView> bookedDatas;

}
