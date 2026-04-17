package com.example.demo.iface.rest;

import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.base.infra.context.ContextHolder;
import com.example.demo.base.shared.event.BaseEvent;
import com.example.demo.iface.dto.req.UploadTemplateResource;
import com.example.demo.infra.blob.MinioService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RequestMapping("/api/v1/test")
@RestController
@AllArgsConstructor
public class TestController {

	private MinioService minioService;

	@PostMapping("/checkContextHolder")
	public String checkToken() {
		BaseEvent event = BaseEvent.builder().eventLogUuid(UUID.randomUUID().toString()).build();
		ContextHolder.setBaseEvent(event);

		log.info("Event: {}", ContextHolder.getEvent());
		log.info("Username:{} ", ContextHolder.getUsername());
		log.info("JwToken: {} ", ContextHolder.getJwtoken());
		return "End";
	}

	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public String upload(@RequestPart(name = "resource", required = true) UploadTemplateResource resource,
			@RequestPart(name = "file", required = true) MultipartFile file) throws Exception {
		minioService.uploadFile(file, resource.getFileName(), resource.getFilePath());
		return "End";
	}

}
