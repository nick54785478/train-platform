package com.example.demo.domain.share;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimetableGeneratedData {

	private TemplateQueriedData templateQueriedData;

	private Map<String, Object> parameters;

	private List<TimetableDetailGeneratedData> details;

}
