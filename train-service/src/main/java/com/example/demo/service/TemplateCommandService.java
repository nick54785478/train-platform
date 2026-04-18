package com.example.demo.service;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.base.application.service.BaseApplicationService;
import com.example.demo.base.shared.enums.YesNo;
import com.example.demo.domain.template.aggregate.Template;
import com.example.demo.domain.template.aggregate.vo.FileType;
import com.example.demo.domain.template.aggregate.vo.TemplateType;
import com.example.demo.domain.template.command.UploadTemplateCommand;
import com.example.demo.infra.blob.MinioService;
import com.example.demo.infra.repository.TemplateRepository;

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
	private TemplateRepository templateRepository;

	/**
	 * 上傳範本資料
	 * 
	 * @param command
	 * @param file
	 * @throws Exception
	 */
	public void upload(UploadTemplateCommand command, MultipartFile file) throws Exception {
		// 取出最新版本
		Template template = templateRepository.findByTypeAndFileTypeAndDeleteFlag(
				TemplateType.valueOf(command.getType()), FileType.fromLabel(command.getFileType()), YesNo.N);
		if (Objects.isNull(template)) {
			// 新增 Template 資料
			Template entity = new Template();
			entity.create(command);
			templateRepository.save(entity);
		} else {
			// TODO 可改為版本控制，但會變得很複雜
		}

		// 上傳 範本資料
		minioService.uploadFile(file, command.getFileName(), command.getFilePath());
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
		Template template = templateRepository.findByTypeAndDeleteFlag(TemplateType.valueOf(type), YesNo.N);
		InputStream inputStream = minioService.downloadFile(template.getFilePath() + "/" + template.getFileName());
		downloadFileMap.put(template.getFileName(), inputStream);
		return downloadFileMap;
	}

}
