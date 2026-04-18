package com.example.demo.domain.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomisationDetailView {
	private Long id;

	private String label;

	private String value;

	private String labelTw;

	private String labelCn;

	private String labelUs;
	

	public CustomisationDetailView(Long id, String label, String value) {
		this.id = id;
		this.label = label;
		this.value = value;
	}
}
