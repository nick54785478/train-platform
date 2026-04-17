package com.example.demo.infra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.domain.ticket.aggregate.Ticket;
import java.util.List;


@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
	
	Ticket findByTicketNo(String ticketNo);
	
	List<Ticket> findByTrainUuid(String trainUuid);
	
	List<Ticket> findByTrainUuidIn(List<String> trainNoList);
	
	List<Ticket> findByTrainUuidAndFromStop(String trainUuid, String fromStop);
	
	List<Ticket> findByTicketNoIn(List<String> ticketNoList);

	List<Ticket> findByFromStopAndToStop(String fromStop, String toStop);
	
	Ticket findByTrainUuidAndFromStopAndToStop(String uuid, String fromStop, String toStop);
}
