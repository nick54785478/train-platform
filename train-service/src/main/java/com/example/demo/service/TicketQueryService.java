package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.base.application.service.BaseApplicationService;
import com.example.demo.domain.service.TicketService;
import com.example.demo.domain.share.TicketQueriedData;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class TicketQueryService extends BaseApplicationService {

	private TicketService ticketService;
	
	/**
	 * 根據車次查詢車票資料
	 * 
	 * @param trainNo
	 * @return List<TicketQueriedData>
	 */
	public List<TicketQueriedData> queryTicketsByTrainNo(Integer trainNo) {
		return ticketService.queryTicketsByTrainNo(trainNo);
	}
}
