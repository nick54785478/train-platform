package com.example.demo.domain.customisation.aggregate.vo;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum CustomisationType {

	SETTING_TABLE_COLUMN("SETTING_TABLE_COLUMN", "Setting 表格欄位查看設定"),
	TRAIN_TABLE_COLUMN("TRAIN_TABLE_COLUMN", "Train 表格欄位查看設定");

	@Getter
	private final String code;
	@Getter
	private final String label;

	// enum 轉 Map
	private static final Map<String, CustomisationType> labelToTypeMap = new HashMap<>();

	static {
		for (CustomisationType type : CustomisationType.values()) {
			labelToTypeMap.put(type.label, type);
		}
	}

	public static CustomisationType fromLabel(String label) {
		return labelToTypeMap.get(label);
	}

	public static Map<String, CustomisationType> getMap() {
		return labelToTypeMap;
	}

	public static Boolean checkCustomisationType(String label) {
		CustomisationType kind = CustomisationType.fromLabel(label);
		if (kind == null) {
			return false;
		}
		return true;
	}
}
