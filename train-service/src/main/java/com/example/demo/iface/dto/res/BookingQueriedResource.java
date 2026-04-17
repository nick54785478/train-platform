package com.example.demo.iface.dto.res;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingQueriedResource {

	private String username; // 使用者帳號

	private List<BookQueriedResource> bookedDatas; // 訂票資訊

}
