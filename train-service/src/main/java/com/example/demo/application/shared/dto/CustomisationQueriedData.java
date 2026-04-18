package com.example.demo.application.shared.dto;

import java.util.List;

import com.example.demo.domain.share.dto.CustomisationDetailView;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomisationQueriedData {

	private String username;

	private List<CustomisationDetailView> value;
}
