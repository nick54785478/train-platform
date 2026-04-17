package com.example.demo.domain.setting.aggregate;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.example.demo.base.shared.enums.YesNo;
import com.example.demo.domain.setting.command.CreateSettingCommand;
import com.example.demo.domain.setting.command.UpdateSettingCommand;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "SETTING")
@EntityListeners(AuditingEntityListener.class)
public class ConfigurableSetting {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	private Long id;
	
	@Column(name = "DATA_TYPE")
	private String dataType;	// 資料類型

	@Column(name = "TYPE")
	private String type; // 種類

	@Column(name = "NAME")
	private String name; // 名稱
	
	@Column(name = "VALUE")
	private String value; // 名稱
	
	@Column(name = "PRIORITY_NO")
	private Integer priorityNo; // 排序

	@Column(name = "DESCRIPTION")
	private String description; // 敘述

	@Enumerated(EnumType.STRING)
	@Column(name = "ACTIVE_FLAG")
	private YesNo activeFlag = YesNo.Y; // 是否有效

	/**
	 * 建立一筆 Setting
	 * 
	 * @param command
	 */
	public void create(CreateSettingCommand command) {
		this.dataType = command.getDataType();
		this.type = command.getType();
		this.name = command.getName();
		this.value = command.getValue();
		this.description = command.getDescription();
		this.priorityNo = command.getPriorityNo();
		this.activeFlag = YesNo.Y;
	}
	
	/**
	 * 修改一筆 Setting
	 * 
	 * @param command
	 */
	public void update(UpdateSettingCommand command) {
		this.dataType = command.getDataType();
		this.type = command.getType();
		this.name = command.getName();
		this.value = command.getValue();
		this.priorityNo = command.getPriorityNo();
		this.description = command.getDescription();
		this.activeFlag = YesNo.valueOf(command.getActiveFlag());
	}
	
	/**
	 * 刪除 (更改 activeFlag = 'N')
	 * */
	public void delete() {
		this.activeFlag = YesNo.N;
	}
}
