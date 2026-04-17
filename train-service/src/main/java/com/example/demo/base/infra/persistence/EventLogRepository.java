package com.example.demo.base.infra.persistence;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.base.shared.entity.EventLog;
import com.example.demo.base.shared.enums.EventLogSendQueueStatus;

@Repository
public interface EventLogRepository extends JpaRepository<EventLog, Long> {

	EventLog findByUuid(String uuid);

	List<EventLog> findByStatusAndOccuredAtBefore(EventLogSendQueueStatus status, Date time);

	List<EventLog> findTop100ByStatus(EventLogSendQueueStatus initial);

}
