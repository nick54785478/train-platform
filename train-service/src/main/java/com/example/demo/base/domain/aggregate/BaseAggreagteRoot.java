package com.example.demo.base.domain.aggregate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.example.demo.base.shared.event.BaseEvent;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import lombok.Getter;

/**
 * 抽象基礎實體類，此類包含一些通用的欄位，如: 創建時間、創建者、更新時間和更新者等。
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseAggreagteRoot {

	@CreatedDate
	@Column(name = "CREATED_DATE")
	private Date createdDate; // 創建時間

	@CreatedBy
	@Column(name = "CREATED_BY")
	private String createdBy; // 創建者

	@LastModifiedDate
	@Column(name = "LAST_UPDATED_DATE")
	private Date lastUpdatedDate; // 最後異動時間

	@LastModifiedBy
	@Column(name = "LAST_UPDATED_BY")
	private String lastUpdatedBy; // 最後異動者

	/**
	 * 聚合根內部事件清單
	 */
	@Transient
	private final List<DomainEvent> domainEvents = new ArrayList<>();

	/**
	 * 本次業務行為的統一代號
	 */
	@Transient
	private String eventTxId;

	/**
	 * 支援「外部傳入 eventId」（跨 Aggregate 共用）
	 */
	public void assignEventTxId(String eventTxId) {
		this.eventTxId = eventTxId;
	}

	/**
	 * 產生新事件並加入清單
	 */
	protected void raiseEvent(DomainEvent event) {
		// 1. 如果事件本身已經有 ID (從 Builder 傳進來的)
		if (event.getEventTxId() != null) {
			// 同步給實體，確保實體與事件一致
			this.eventTxId = event.getEventTxId();
		}
		// 2. 如果事件沒 ID，且實體也沒 ID，才生成新的
		else if (this.eventTxId == null) {
			this.eventTxId = UUID.randomUUID().toString();
		}

		// 3. 最終確保事件掛載的是正確的 ID
		event.setEventTxId(this.eventTxId);
		domainEvents.add(event);
	}

	/**
	 * 取得事件清單
	 */
	public List<DomainEvent> getDomainEvents() {
		return Collections.unmodifiableList(domainEvents);
	}

	/**
	 * 清理事件
	 */
	public void clearDomainEvents() {
		domainEvents.clear();
		eventTxId = null;
	}

}
