package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.base.application.service.BaseApplicationService;
import com.example.demo.domain.service.TicketService;
import com.example.demo.domain.ticket.command.CreateOrUpdateTicketCommand;
import com.example.demo.domain.ticket.command.CreateTicketCommand;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, timeout = 36000, rollbackFor = Exception.class)
public class TicketCommandService extends BaseApplicationService {

	private final TicketService ticketService;

	/**
	 * 新增車票資訊
	 * 
	 * @param command
	 */
	public void createTicket(CreateTicketCommand command) {
		ticketService.create(command);
	}

	/**
	 * 批次新增車票資訊
	 * 
	 * @param trainNo  車次
	 * @param commands
	 */
	public void createOrUpdateTickets(Integer trainNo, List<CreateOrUpdateTicketCommand> commands) {
		ticketService.createOrUpdate(trainNo, commands);
	}

}
