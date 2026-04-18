package com.example.demo.iface.rest;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Objects;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.application.shared.dto.TemplateQueriedData;
import com.example.demo.base.shared.exception.exception.ValidationException;
import com.example.demo.domain.template.command.UploadTemplateCommand;
import com.example.demo.iface.dto.req.UploadTemplateResource;
import com.example.demo.iface.dto.res.TemplateQueriedResource;
import com.example.demo.iface.dto.res.TemplateUploadedResource;
import com.example.demo.service.TemplateCommandService;
import com.example.demo.service.TemplateQueryService;
import com.example.demo.util.BaseDataTransformer;

import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/template")
@Tag(name = "Template API", description = "進行與範本領域相關動作")
public class TemplateController {

	private TemplateQueryService templateQueryService;
	private TemplateCommandService templateCommandService;

	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "API - 上傳範本資料", description = "上傳範本資料。")
	public ResponseEntity<TemplateUploadedResource> uploadTemplate(
			@RequestPart(name = "resource", required = true) UploadTemplateResource resource,
			@RequestPart(name = "file", required = true) MultipartFile file) {
		UploadTemplateCommand command = BaseDataTransformer.transformData(resource, UploadTemplateCommand.class);
		try {
			templateCommandService.upload(command, file);
		} catch (Exception e) {
			log.error("發生錯誤，上傳失敗", e);
			throw new ValidationException("VALIDATE_FAILED", "發生錯誤，上傳失敗");
		}
		return new ResponseEntity<>(new TemplateUploadedResource("201", "上傳成功"), HttpStatus.OK);
	}

	@GetMapping("")
	@Operation(summary = "API - 查詢特定種類範本", description = "查詢特定種類範本。")
	public ResponseEntity<TemplateQueriedResource> queryByType(
			@Parameter(description = "範本種類") @RequestParam String type) {
		TemplateQueriedData queriedData = templateQueryService.queryByType(type);
		TemplateQueriedResource resource = Objects.isNull(queriedData) ? null
				: BaseDataTransformer.transformData(queriedData, TemplateQueriedResource.class);
		return new ResponseEntity<>(resource, HttpStatus.OK);
	}

	@GetMapping(value = "/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	@Operation(summary = "API - 下載範本", description = "下載範本。")
	public ResponseEntity<Resource> download(@Parameter(description = "範本種類") @RequestParam String type) {
		try {
			Map<String, InputStream> downloadFileMap = templateCommandService.download(type);
			String fileName = null;
			InputStream inputStream = null;
			// 迴圈取出 檔案名稱 及 inputStream，通常也只會有一筆。
			for (String key : downloadFileMap.keySet()) {
				fileName = key;
				if (downloadFileMap.isEmpty() || Objects.isNull(downloadFileMap.get(key))) {
					log.error("未取得檔案:{}，導致錯誤。", fileName);
					throw new ValidationException("VALIDATE_FAILED", "未取得檔案 " + fileName + "，導致錯誤。");
				}
				inputStream = downloadFileMap.get(key);
			}

			// 設置檔名進 Header
//			 String encodedFileName = UriUtils.encode(fileName, StandardCharsets.UTF_8);
			// 待確認是否加密
			ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
					.filename(fileName, StandardCharsets.UTF_8).build();
			HttpHeaders respHeaders = new HttpHeaders();
			respHeaders.setContentDisposition(contentDisposition);

			return new ResponseEntity<>(new InputStreamResource(inputStream), respHeaders, HttpStatus.OK);

		} catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
				| InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
				| IllegalArgumentException | IOException e) {
			log.error("發生錯誤，下載失敗", e);
			throw new ValidationException("VALIDATE_FAILED", "發生錯誤，下載失敗");
		}
	}

}
