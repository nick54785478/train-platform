package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.base.application.service.BaseApplicationService;
import com.example.demo.domain.service.TicketService;
import com.example.demo.domain.share.dto.TicketQueriedView;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class TicketQueryService extends BaseApplicationService {

	private TicketService ticketService;
	
	/**
	 * 根據車次查詢車票資料
	 * 
	 * @param trainNo
	 * @return List<TicketQueriedView>
	 */
	public List<TicketQueriedView> queryTicketsByTrainNo(Integer trainNo) {
		return ticketService.queryTicketsByTrainNo(trainNo);
	}
}
