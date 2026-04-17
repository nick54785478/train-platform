package com.example.demo.domain.account.aggregate.vo;

public enum AccountStatus {
	PENDING, // 待處理（註冊成功，但初始儲值尚未確認）
	ACTIVE, // 已啟用（初始儲值成功，可正常交易）
	FROZEN, // 已凍結（初始儲值失敗，帳號鎖定，需人工或補償機制解鎖）
	CLOSED // 已結案/註銷
}
