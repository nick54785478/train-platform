package com.example.demo.iface.dto.res;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomisationQueriedResource {

	private String username;
	
	private List<OptionQueriedResource> value;
}
