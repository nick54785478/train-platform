package com.example.demo.base.application.port;

import com.example.demo.base.shared.command.BaseIdempotentCommand;
import com.example.demo.base.shared.event.BaseEvent;

public interface EventIdempotenceHandlerPort {

	/**
	 * 執行 Event 的冪等機制
	 * 
	 * @param event
	 * @return boolean
	 */
	public boolean handleIdempotency(BaseEvent event);

	/**
	 * 執行 非 Event 的冪等機制
	 * 
	 * @param command
	 * @return boolean
	 */
	public boolean handleIdempotency(BaseIdempotentCommand command);

}
