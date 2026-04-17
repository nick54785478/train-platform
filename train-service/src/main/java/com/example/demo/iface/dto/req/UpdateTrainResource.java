package com.example.demo.iface.dto.req;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "火車車次資訊")
public class UpdateTrainResource {

	@Schema(description = "火車代號")
	private Integer trainNo; // 火車代號

	@Schema(description = "停靠站清單")
	private List<UpdateStopResource> stops;
}
