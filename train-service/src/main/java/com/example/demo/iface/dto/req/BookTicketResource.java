package com.example.demo.iface.dto.req;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "車票預定資訊")
public class BookTicketResource {

	@Schema(description = "火車代號 uuid")
	private String trainUuid; // 火車 UUID

	@Schema(description = "車票代號 uuid")
	private String ticketUuid; // 車票 UUID

	@Schema(description = "火車號次")
	private Integer trainNo; // 火車號次

	@Schema(description = "價格")
	private BigDecimal price; // 價格

	@Schema(description = "座號")
	private String seatNo; // 座號

	@Schema(description = "車廂編號")
	private Long carNo;

	@Schema(description = "乘車日期 (yyyy/mm/dd)")
	private String takeDate; // 乘車日期

	@Schema(description = "付款方式")
	private String payMethod; // 付款方式
}
