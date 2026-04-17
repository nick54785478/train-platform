package com.example.demo.domain.share;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoneyAccountQueriedData {

	private String uuid; // 使用者 uuid

	private String name; // 人名

	private String username; // 帳號

	private String email; // email

	private BigDecimal balance = new BigDecimal("0"); // 餘額
}
