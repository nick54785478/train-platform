package com.example.demo.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 取號工具
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SequenceGenerator {

	private static final int MIN_NUMBER = 1; // 最小數字
	private static final int MAX_NUMBER = 13; // 最大數字
	private static final char START_LETTER = 'A'; // 開始字母
	private static final char END_LETTER = 'E'; // 結束字母
	private static final Random RANDOM = new Random();

	/**
	 * 隨機取得一個號碼 (數字 + 英文字母)
	 * 
	 * @return 隨機號碼
	 */
	public static String generateSeatNumber() {
		int number = RANDOM.nextInt(MAX_NUMBER - MIN_NUMBER + 1) + MIN_NUMBER; // 隨機數字
		char letter = (char) (RANDOM.nextInt(END_LETTER - START_LETTER + 1) + START_LETTER); // 隨機字母
		return number + String.valueOf(letter);
	}

	/**
	 * 生成火車座位號
	 * 
	 * @param rows    行數 (例如 1~13)
	 * @param columns 列數 (例如 A~E)
	 * @return 座位號列表
	 */
	public static List<String> generateSeats(int rows, char startColumn, char endColumn) {
		List<String> seatList = new ArrayList<>();

		// 循環生成每個行和列的座位號
		for (int i = 1; i <= rows; i++) {
			for (char column = startColumn; column <= endColumn; column++) {
				seatList.add(i + String.valueOf(column));
			}
		}
		return seatList;
	}
}
