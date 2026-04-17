package com.example.demo.domain.template.aggregate.vo;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 檔案類型
 */
@AllArgsConstructor
public enum FileType {

	PDF("pdf", "pdf"),

	XLSX("xlsx", "xlsx"),

	TXT("txt", "txt"),

	JASPER("jasper", "jasper"),
	
	DOC("doc", "doc");

	@Getter
	private final String code;
	@Getter
	private final String label;

	// enum 轉 Map
	private static final Map<String, FileType> labelToTypeMap = new HashMap<>();

	static {
		for (FileType type : FileType.values()) {
			labelToTypeMap.put(type.label, type);
		}
	}

	public static FileType fromLabel(String label) {
		return labelToTypeMap.get(label);
	}

	public static Map<String, FileType> getMap() {
		return labelToTypeMap;
	}

	public static Boolean checkTrainKind(String label) {
		FileType kind = FileType.fromLabel(label);
		if (kind == null) {
			return false;
		}
		return true;
	}

}