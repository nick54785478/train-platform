package com.example.demo.base.domain.aggregate;

import java.util.Date;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

/**
 * 抽象基礎實體類，此類包含一些通用的欄位，如: 創建時間、創建者、更新時間和更新者等。
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

	@CreatedDate
	@Column(name = "CREATED_DATE")
	private Date createdDate;	// 創建時間

	@CreatedBy
	@Column(name = "CREATED_BY")
	private String createdBy;	// 創建者

	@LastModifiedDate
	@Column(name = "LAST_UPDATED_DATE")
	private Date lastUpdatedDate;	// 最後異動時間

	@LastModifiedBy
	@Column(name = "LAST_UPDATED_BY")
	private String lastUpdatedBy;	// 最後異動者

}
