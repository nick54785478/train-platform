package com.example.demo.domain.template.aggregate.vo;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 範本類型
 */
@AllArgsConstructor
public enum TemplateType {

	TRAIN_TIMETABLE("TRAIN_TIMETABLE", "火車時刻表範本"),
	
	SINGLE_TRAIN_TIMETABLE("SINGLE_TRAIN_TIMETABLE", "單一車次時刻表範本"),

	TRAIN_UPLOAD("TRAIN_UPLOAD", "車次資料上傳範本");

	@Getter
	private final String code;
	@Getter
	private final String label;

	// enum 轉 Map
	private static final Map<String, TemplateType> labelToTypeMap = new HashMap<>();

	static {
		for (TemplateType type : TemplateType.values()) {
			labelToTypeMap.put(type.label, type);
		}
	}

	public static TemplateType fromLabel(String label) {
		return labelToTypeMap.get(label);
	}

	public static Map<String, TemplateType> getMap() {
		return labelToTypeMap;
	}

	public static Boolean checkTrainKind(String label) {
		TemplateType kind = TemplateType.fromLabel(label);
		if (kind == null) {
			return false;
		}
		return true;
	}

}