package com.example.demo.domain.service;

import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.base.domain.service.BaseDomainService;
import com.example.demo.base.shared.enums.YesNo;
import com.example.demo.domain.share.TemplateQueriedData;
import com.example.demo.domain.template.aggregate.Template;
import com.example.demo.domain.template.aggregate.vo.FileType;
import com.example.demo.domain.template.aggregate.vo.TemplateType;
import com.example.demo.domain.template.command.UploadTemplateCommand;
import com.example.demo.infra.blob.MinioService;
import com.example.demo.infra.repository.TemplateRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class TemplateService extends BaseDomainService {

	private MinioService minioService;
	private TemplateRepository templateRepository;

	/**
	 * 上傳範本
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
	 * 查詢 Template 資料
	 * 
	 * @param type
	 * @return TemplateQueriedData
	 */
	public TemplateQueriedData queryByType(String type) {
		Template queried = templateRepository.findByTypeAndDeleteFlag(TemplateType.valueOf(type), YesNo.N);
		return Objects.isNull(queried) ? null : transformEntityToData(queried, TemplateQueriedData.class);
	}

}
