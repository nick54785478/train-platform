package com.example.demo.iface.dto.req;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCustomizedValueResource {

	private String username;

	private String dataType;

	private String type;

	private List<String> valueList;
}
