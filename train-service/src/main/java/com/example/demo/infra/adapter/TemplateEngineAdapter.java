package com.example.demo.infra.adapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import com.example.demo.application.port.TemplateEnginePort;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.Version;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
class TemplateEngineAdapter implements TemplateEnginePort {

	private final Configuration configuration;

	public TemplateEngineAdapter() {
		// 將原本 static 的初始化邏輯移至建構子
		this.configuration = new Configuration(new Version("2.3.31"));
		this.configuration.setDefaultEncoding("UTF-8");
	}

	@Override
	public String renderFromString(String templateName, String templateContent, Map<String, Object> model) {
		if (templateContent == null || templateContent.isBlank()) {
			return null;
		}

		try (StringReader reader = new StringReader(templateContent)) {
			Template template = new Template(templateName, reader, configuration);
			return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
		} catch (IOException | TemplateException e) {
			log.error("FreeMarker 渲染失敗 (String), 模板名稱: {}", templateName, e);
			throw new RuntimeException("模板渲染異常", e);
		}
	}

	@Override
	public String renderFromStream(String templateName, InputStream inputStream, Map<String, Object> model) {
		try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
			Template template = new Template(templateName, reader, configuration);
			return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
		} catch (IOException | TemplateException e) {
			log.error("FreeMarker 渲染失敗 (Stream), 模板名稱: {}", templateName, e);
			throw new RuntimeException("模板渲染異常", e);
		}
	}
}