package com.example.demo.domain.shared.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum TrainSheetMapping {

	TRAIN("Train"), STOPS("Stops"), TICKETS("Tickets");
	
	@Getter
	private String name;
}
