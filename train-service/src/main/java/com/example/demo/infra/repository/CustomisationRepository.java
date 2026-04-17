package com.example.demo.infra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.base.shared.enums.YesNo;
import com.example.demo.domain.customisation.aggregate.Customisation;
import com.example.demo.domain.customisation.aggregate.vo.CustomisationType;

@Repository
public interface CustomisationRepository extends JpaRepository<Customisation, Long> {

	Customisation findByUsernameAndTypeAndNameAndActiveFlag(String username, CustomisationType type, String name,
			YesNo activeFlag);

	Customisation findByUsernameAndTypeAndActiveFlag(String username, CustomisationType type, YesNo activeFlag);
}
