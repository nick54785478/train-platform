package com.example.demo.infra.repository;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.base.shared.enums.YesNo;
import com.example.demo.domain.setting.aggregate.ConfigurableSetting;

import jakarta.persistence.criteria.Predicate;

@Repository
public interface SettingRepository extends JpaRepository<ConfigurableSetting, Long> {
	
	List<ConfigurableSetting> findByTypeAndActiveFlag(String type, YesNo activeFlag);
	
	List<ConfigurableSetting> findByDataTypeAndTypeAndActiveFlag(String dataType, String type, YesNo activeFlag);

	List<ConfigurableSetting> findByDataTypeAndActiveFlag(String dataType, YesNo activeFlag);

	List<ConfigurableSetting> findAll(Specification<ConfigurableSetting> specification);

	default List<ConfigurableSetting> findAllWithSpecification(String dataType, String type, String name,
			String activeFlag) {
		Specification<ConfigurableSetting> specification = ((root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (StringUtils.isNotBlank(dataType)) {
				predicates.add(cb.equal(root.get("dataType"), dataType));
			}

			if (StringUtils.isNotBlank(type)) {
				predicates.add(cb.like(root.get("type"), "%" + type + "%"));
			}

			if (StringUtils.isNotBlank(name)) {
				predicates.add(cb.like(root.get("name"), name));
			}

			if (StringUtils.isNotBlank(activeFlag)) {
				predicates.add(cb.equal(root.get("activeFlag"), activeFlag));
			} else {
				predicates.add(cb.equal(root.get("activeFlag"), "Y"));
			}

			Predicate[] predicateArray = new Predicate[predicates.size()];
			query.where(cb.and(predicates.toArray(predicateArray)));
			return query.getRestriction();
		});
		return findAll(specification);
	}

}
