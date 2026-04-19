package com.example.demo.base.application.port;

import com.example.demo.base.shared.command.ExecuteIdempotenceCommand;

public interface BusinessIdempotenceExecutorPort {
	/**
	 * 嘗試鎖定或執行具備冪等特徵的指令
	 * 
	 * @return true 代表成功取得執行權（拿到位置），false 代表資源已被他人搶走
	 */
	boolean execute(ExecuteIdempotenceCommand command);
}