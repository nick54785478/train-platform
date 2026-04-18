package com.example.demo.domain.share;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomisationQueriedData {

	private String username;

	private List<OptionQueriedData> value;
}
