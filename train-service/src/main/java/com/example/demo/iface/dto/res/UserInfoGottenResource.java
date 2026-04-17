package com.example.demo.iface.dto.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoGottenResource {

	private Long id;

	private String name;

	private String email;

	private String address;

}
