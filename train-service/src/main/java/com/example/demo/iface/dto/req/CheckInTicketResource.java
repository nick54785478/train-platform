package com.example.demo.iface.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "車票 Check IN 資訊")
public class CheckInTicketResource {

	@Schema(description = "預定代號 uuid")
	private String uuid; // booking uuid

	@Schema(description = "座號")
	private String seatNo; // 座號

	@Schema(description = "車廂編號")
	private Long carNo; // 車廂編號

	@Schema(description = "搭乘日期 (yyyy/mm/dd)")
	private String takeDate; // 搭乘日期

}
