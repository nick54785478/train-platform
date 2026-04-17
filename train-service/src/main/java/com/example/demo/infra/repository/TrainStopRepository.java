package com.example.demo.infra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.domain.train.aggregate.entity.TrainStop;

@Repository
public interface TrainStopRepository extends JpaRepository<TrainStop, String> {

}
