package com.example.demo.service;

import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.application.shared.dto.TemplateQueriedData;
import com.example.demo.base.application.service.BaseApplicationService;
import com.example.demo.base.shared.enums.YesNo;
import com.example.demo.domain.template.aggregate.Template;
import com.example.demo.domain.template.aggregate.vo.TemplateType;
import com.example.demo.infra.repository.TemplateRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class TemplateQueryService extends BaseApplicationService {

	private TemplateRepository templateRepository;

	/**
	 * 根據範本種類條件查詢
	 * 
	 * @param type
	 * @return TemplateQueriedData
	 */
	public TemplateQueriedData queryByType(String type) {
		Template queried = templateRepository.findByTypeAndDeleteFlag(TemplateType.valueOf(type), YesNo.N);
		return Objects.isNull(queried) ? null : this.transformData(queried, TemplateQueriedData.class);

	}

}
