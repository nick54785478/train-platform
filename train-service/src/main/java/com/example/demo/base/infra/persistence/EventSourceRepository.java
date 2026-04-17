package com.example.demo.base.infra.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.base.shared.entity.EventSource;


@Repository
public interface EventSourceRepository extends JpaRepository<EventSource, Long> {

	EventSource findTopByUuidOrderByVersionDesc(String uuid);
	
}
