package com.example.demo.application.port;

import java.io.InputStream;
import java.util.Map;

/**
 * 模板引擎 Port 負責將資料模型渲染進指定模板。
 */
public interface TemplateEnginePort {

	/**
	 * 從字串模板渲染
	 */
	String renderFromString(String templateName, String templateContent, Map<String, Object> model);

	/**
	 * 從 InputStream 模板渲染
	 */
	String renderFromStream(String templateName, InputStream inputStream, Map<String, Object> model);
}