package com.example.demo.domain.shared.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomisationQueriedView {

	private String username;

	private List<CustomisationDetailView> value;
}
