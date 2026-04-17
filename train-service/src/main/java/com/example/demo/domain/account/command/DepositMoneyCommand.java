package com.example.demo.domain.account.command;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositMoneyCommand {

	private String uuid;

	private BigDecimal money; // 金額
}
