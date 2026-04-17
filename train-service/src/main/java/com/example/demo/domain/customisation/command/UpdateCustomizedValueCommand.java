package com.example.demo.domain.customisation.command;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCustomizedValueCommand {

	private String username;

	private String dataType;

	private String type;

	private List<String> valueList;
}
