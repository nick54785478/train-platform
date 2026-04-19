package com.example.demo.base.infra.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.base.shared.entity.BusinessIdempotenceLog;

public interface BusinessIdempotenceRepository extends JpaRepository<BusinessIdempotenceLog, Long> {
}