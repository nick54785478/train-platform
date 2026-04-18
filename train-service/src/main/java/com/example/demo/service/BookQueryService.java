package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.domain.service.TicketBookingService;
import com.example.demo.domain.share.dto.BookingQueriedView;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class BookQueryService {

	private TicketBookingService ticketBookingService;

	/**
	 * 查詢個人預定班次
	 * 
	 * @param username 使用者帳號
	 */
	public BookingQueriedView queryBooking(String username) {
		return ticketBookingService.queryBooking(username);
	}
}
