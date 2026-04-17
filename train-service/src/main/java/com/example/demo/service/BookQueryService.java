package com.example.demo.service;

import org.springframework.stereotype.Service;

import com.example.demo.domain.service.TicketBookingService;
import com.example.demo.domain.share.BookingQueriedData;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class BookQueryService {

	private TicketBookingService ticketBookingService;

	/**
	 * 查詢個人預定班次
	 * */
	public BookingQueriedData queryBooking(String username) {
		return ticketBookingService.queryBooking(username);
	}
}
