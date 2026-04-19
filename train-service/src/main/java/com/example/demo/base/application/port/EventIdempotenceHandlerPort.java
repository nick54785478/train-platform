package com.example.demo.base.application.port;

import com.example.demo.base.shared.event.BaseEvent;

/**
 * Port 定義： 冪等機制介面
 * 
 * <p>
 * 應用層使用此接口進行物件與物件之間的資料轉換，不依賴具體實作細節。
 * </p>
 */
public interface EventIdempotenceHandlerPort {

	/**
	 * 執行 Event 的冪等機制
	 * 
	 * @param event
	 * @return boolean
	 */
	public boolean handleIdempotency(BaseEvent event);

}
