package com.example.demo.domain.share.enums;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
public enum TicketType {
	
	ADULT("ADULT", "全票"),

	EARLY_BIRD("EARLY_BIRD", "早鳥票"),
	
	MILITARY_POLICE("MILITARY_POLICE", "軍警票"),
	
	SENIOR_PERSONS("SENIOR_PERSONS", "敬老票"),

	STUDENT("STUDENT", "學生票");
	
	

	@Getter
	private final String code;
	@Getter
	private final String label;


	// enum 轉 Map
	private static final Map<String, TicketType> labelToTypeMap = new HashMap<>();

	static {
		for (TicketType type : TicketType.values()) {
			labelToTypeMap.put(type.label, type);
		}
	}

	public static TicketType fromLabel(String label) {
		return labelToTypeMap.get(label);
	}
	
	public static Map<String, TicketType> getMap() {
		return labelToTypeMap;
	}
	
	public static Boolean checkTrainKind(String label) {
		TicketType kind = TicketType.fromLabel(label);
		if (kind == null) {
			return false;
		}
		return true;
	}
	

}