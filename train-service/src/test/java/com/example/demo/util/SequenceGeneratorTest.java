package com.example.demo.util;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SequenceGeneratorTest {

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
	void testGenerateSeatNumber() {
	}

	@Test
	void testGenerateSeats() {
		// 設置行數和列範圍
		int rows = 13; // 行數 1~13
		char startColumn = 'A'; // 開始列 A
		char endColumn = 'E'; // 結束列 E
		// 生成座位號
		List<String> seats = SequenceGenerator.generateSeats(rows, startColumn, endColumn);

		// 輸出座位號
		seats.forEach(System.out::println);
	}

}
