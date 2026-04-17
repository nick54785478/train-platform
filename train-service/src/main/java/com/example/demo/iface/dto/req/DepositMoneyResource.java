package com.example.demo.iface.dto.req;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "儲值資訊")
public class DepositMoneyResource {

	@Schema(description = "帳號代號 uuid")
	private String uuid;	// 帳號 UUID
	
	@Schema(description = "金額")
	private BigDecimal money; // 金額
}
