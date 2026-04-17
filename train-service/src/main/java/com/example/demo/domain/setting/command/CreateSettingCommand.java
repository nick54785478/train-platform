package com.example.demo.domain.setting.command;

import com.example.demo.base.shared.enums.YesNo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSettingCommand {
	
	private String dataType; // 資料種類

	private String type; // 種類

	private String name; // 名稱

	private String value; // 值
	
	private String description; // 敘述

	private Integer priorityNo; // 順序號(從 1 開始)

	private YesNo activeFlag = YesNo.Y; // 是否有效
}
