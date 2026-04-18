package com.example.demo.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.base.application.service.BaseApplicationService;
import com.example.demo.domain.service.TicketService;
import com.example.demo.domain.shared.dto.TicketQueriedView;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
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
