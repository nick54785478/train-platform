package com.example.demo.base.shared.enums;

/**
 * 管理 EventLog 的 Event 狀態
 */
public enum EventLogSendQueueStatus {

	INITIAL(0), SENT(1), CONSUMED(2);

	private final int value;

	private EventLogSendQueueStatus(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

	public boolean sameValueAs(EventLogSendQueueStatus other) {
		return other != null && this.equals(other);
	}
}