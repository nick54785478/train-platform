package com.example.demo.service;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.base.application.service.BaseApplicationService;
import com.example.demo.domain.service.TemplateService;
import com.example.demo.domain.share.TemplateQueriedData;
import com.example.demo.domain.template.command.UploadTemplateCommand;
import com.example.demo.infra.blob.MinioService;

import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, timeout = 36000, rollbackFor = Exception.class)
public class TemplateCommandService extends BaseApplicationService {

	private MinioService minioService;
	private TemplateService templateService;

	/**
	 * 上傳範本資料
	 * 
	 * @param command
	 * @param file
	 * @throws Exception
	 */
	public void upload(UploadTemplateCommand command, MultipartFile file) throws Exception {
		templateService.upload(command, file);
	}

	/**
	 * 下載範本
	 * 
	 * @param type
	 * @return Map<String, InputStream>
	 */
	public Map<String, InputStream> download(String type) throws InvalidKeyException, ErrorResponseException,
			InsufficientDataException, InternalException, InvalidResponseException, NoSuchAlgorithmException,
			ServerException, XmlParserException, IllegalArgumentException, IOException {
		Map<String, InputStream> downloadFileMap = new HashMap<>();
		TemplateQueriedData template = templateService.queryByType(type);
		InputStream inputStream = minioService.downloadFile(template.getFilePath() + "/" + template.getFileName());
		downloadFileMap.put(template.getFileName(), inputStream);
		return downloadFileMap;
	}

}
