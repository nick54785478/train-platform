package com.example.demo.iface.dto.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OptionQueriedResource {

	private String label;

	private String value;

	private String labelTw;

	private String labelCn;

	private String labelUs;
}
