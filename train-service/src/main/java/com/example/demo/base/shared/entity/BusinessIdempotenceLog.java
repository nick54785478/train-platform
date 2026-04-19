package com.example.demo.base.shared.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "BUSINESS_IDEMPOTENCY_LOG")
public class BusinessIdempotenceLog {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "BUSINESS_KEY", nullable = false, unique = true)
	private String businessKey;

	@Column(name = "TARGET_ID")
	private String targetId;

	@Column(name = "CREATED_AT", nullable = false)
	private LocalDateTime createdAt;
}
