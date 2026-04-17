package com.example.demo.base.application.port;

import java.util.List;

import com.example.demo.base.shared.command.PublishEventCommand;

public interface EventPublisherPort {

	/**
	 * 發布單一事件。
	 *
	 * @param command 要發布的事件命令
	 */
	void publish(PublishEventCommand<?> command);

	/**
	 * 批次發布多個事件。
	 *
	 * @param commands 要發布的事件命令列表
	 */
	void publish(List<PublishEventCommand<?>> commands);

}
