package com.example.demo.iface.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "停靠站資訊")
public class CreateStopResource {

	@Schema(description = "停站順序")
	private Integer seq; // 停站順序

	@Schema(description = "站名")
	private String stopName; // 站名

	@Schema(description = "停靠時間 (hh:mm)")
	private String stopTime; // 停靠時間
}
