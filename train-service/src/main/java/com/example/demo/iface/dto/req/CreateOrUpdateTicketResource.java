package com.example.demo.iface.dto.req;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "車票資訊")
public class CreateOrUpdateTicketResource {

	@Schema(description = "火車車次")
	private Integer trainNo;

	@Schema(description = "起站")
	private String fromStop;

	@Schema(description = "迄站")
	private String toStop;
	
	@Schema(description = "價格")
	private BigDecimal price;
}
