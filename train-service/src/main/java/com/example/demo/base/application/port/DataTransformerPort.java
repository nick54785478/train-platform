package com.example.demo.base.application.port;

import java.util.List;

/**
 * Port 定義：資料轉換服務接口。
 *
 * <p>
 * 應用層使用此接口進行物件與物件之間的資料轉換，不依賴具體實作細節。
 */
public interface DataTransformerPort {

	/**
	 * 單一物件轉換
	 *
	 * @param source 來源物件
	 * @param clazz  目標型別
	 * @param <T>    目標型別泛型
	 * @return 轉換後的物件
	 */
	<T> T transform(Object source, Class<T> clazz);

	/**
	 * List 物件轉換
	 *
	 * @param sourceList 來源物件列表
	 * @param clazz      目標型別
	 * @param <S>        來源物件泛型
	 * @param <T>        目標物件泛型
	 * @return 轉換後的物件列表
	 */
	<S, T> List<T> transformList(List<S> sourceList, Class<T> clazz);

}
