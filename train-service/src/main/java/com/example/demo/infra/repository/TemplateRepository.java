package com.example.demo.infra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.base.shared.enums.YesNo;
import com.example.demo.domain.template.aggregate.Template;
import com.example.demo.domain.template.aggregate.vo.FileType;
import com.example.demo.domain.template.aggregate.vo.TemplateType;

@Repository
public interface TemplateRepository extends JpaRepository<Template, Long> {

	Template findByTypeAndFileTypeAndDeleteFlag(TemplateType templateType, FileType fileType, YesNo deleteFlag);

	Template findByTypeAndDeleteFlag(TemplateType type, YesNo deleteFlag);
}
