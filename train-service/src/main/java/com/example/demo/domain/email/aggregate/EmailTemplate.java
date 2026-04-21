package com.example.demo.domain.email.aggregate;

import com.example.demo.base.domain.aggregate.BaseAggreagteRoot;
import com.example.demo.base.shared.enums.YesNo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "EMAIL_TEMPLATE")
public class EmailTemplate extends BaseAggreagteRoot {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "TEMPLATE_KEY", unique = true, nullable = false)
	private String templateKey;

	@Column(name = "TEMPLATE_Name", nullable = false)
	private String templateName;

	@Column(name = "SUBJECT", nullable = false)
	private String subject;

	@Lob
	@Column(name = "CONTENT", columnDefinition = "TEXT", nullable = false)
	private String content;

	@Enumerated(EnumType.STRING)
	@Column(name = "ACTIVE_FLAG")
	private YesNo activeFlag;

	// --- 業務方法 ---

	/**
	 * 工廠方法：建立新範本
	 */
	public static EmailTemplate create(String key, String name, String subject, String content) {
		return EmailTemplate.builder().templateKey(key)
				.templateName(name).subject(subject).content(content).activeFlag(YesNo.Y).build();
	}

	/**
	 * 更新內容
	 */
	public void update(String subject, String content, YesNo activeFlag) {
		this.subject = subject;
		this.content = content;
		this.activeFlag = activeFlag;
	}

	/**
	 * 邏輯刪除 (停用)
	 */
	public void disable() {
		this.activeFlag = YesNo.N;
	}
}