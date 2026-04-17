package com.example.demo.domain.share.enums;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum PayMethod {

	PAY_BY_ACCOUNT("PAY_BY_ACCOUNT", "帳戶錢包"), PAY_BY_CREDIT_CARD("PAY_BY_CREDIT_CARD", "信用卡"),
	PAY_BY_DEBIT_CARD("PAY_BY_DEBIT_CARD", "簽帳卡");

	@Getter
	private final String code;
	@Getter
	private final String label;

	// enum 轉 Map
	private static final Map<String, PayMethod> labelToTypeMap = new HashMap<>();

	static {
		for (PayMethod type : PayMethod.values()) {
			labelToTypeMap.put(type.label, type);
		}
	}

	public static PayMethod fromLabel(String label) {
		return labelToTypeMap.get(label);
	}

	public static Map<String, PayMethod> getMap() {
		return labelToTypeMap;
	}

	public static Boolean checkPayMethod(String label) {
		PayMethod kind = PayMethod.fromLabel(label);
		if (kind == null) {
			return false;
		}
		return true;
	}
}
