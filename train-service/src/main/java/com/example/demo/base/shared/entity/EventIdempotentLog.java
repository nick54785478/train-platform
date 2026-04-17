package com.example.demo.base.shared.entity;

import java.util.Date;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * EventLog 的 冪等表實體
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IdClass(EventIdempotentLogId.class)
@Table(name = "EVENT_IDEMPOTENT_LOG")
@EntityListeners(AuditingEntityListener.class)
public class EventIdempotentLog {

    @Id
    @Column(name = "UNIQUE_KEY")
    private String uniqueKey;	// 對應 EventLog 的 UUID
    
    @Id
    @Column(name = "EVENT_TYPE")
    private String eventType;	// Event 類型

    @Column(name = "TARGET_ID")
    private String targetId;	// 該事件目標的 UUID (如: 火車等)

    @CreatedDate
    @Column(name = "CREATED_DATE")
    private Date createdDate;	// 建立時間

    public EventIdempotentLog(String eventType, String uniqueKey) {
        this.eventType = eventType;
        this.uniqueKey = uniqueKey;
    }

}
