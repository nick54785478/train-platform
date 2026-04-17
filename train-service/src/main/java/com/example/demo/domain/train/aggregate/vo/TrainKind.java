package com.example.demo.domain.train.aggregate.vo;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
public enum TrainKind {

	JI_XIANG("A", "吉祥號"),

	RU_YI("B", "如意號");

	@Getter
	private final String code;
	@Getter
	private final String label;


	// enum 轉 Map
	private static final Map<String, TrainKind> labelToTypeMap = new HashMap<>();

	static {
		for (TrainKind type : TrainKind.values()) {
			labelToTypeMap.put(type.label, type);
		}
	}

	public static TrainKind fromLabel(String label) {
		return labelToTypeMap.get(label);
	}
	
	public static Map<String, TrainKind> getMap() {
		return labelToTypeMap;
	}
	
	public static Boolean checkTrainKind(String label) {
		TrainKind kind = TrainKind.fromLabel(label);
		if (kind == null) {
			return false;
		}
		return true;
	}
	

}