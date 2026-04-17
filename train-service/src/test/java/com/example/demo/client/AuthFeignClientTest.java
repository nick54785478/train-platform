package com.example.demo.client;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.demo.iface.dto.res.UserInfoGottenResource;
import com.example.demo.infra.client.AuthFeignClient;

@SpringBootTest
class AuthFeignClientTest {

	@Autowired
	AuthFeignClient authFeignClient;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void test() {
		UserInfoGottenResource resource = authFeignClient.getUserByEmail("nick123@example.coms");
		System.out.println("response data: "+resource);
	}

}
