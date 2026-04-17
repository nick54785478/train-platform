package com.example.demo.domain.customisation.aggregate;

import java.util.Objects;

import com.example.demo.base.domain.aggregate.BaseAggreagteRoot;
import com.example.demo.base.infra.context.ContextHolder;
import com.example.demo.base.shared.enums.YesNo;
import com.example.demo.domain.customisation.aggregate.vo.CustomisationType;
import com.example.demo.domain.customisation.command.CreateCustomisationCommand;
import com.example.demo.domain.customisation.command.UpdateCustomisationCommand;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 個人化配置
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "CUSTOMISATION")
@EqualsAndHashCode(callSuper = true)
public class Customisation extends BaseAggreagteRoot {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	private Long id;

	@Column(name = "USERNAME")
	private String username; // 帳號

	@Enumerated(EnumType.STRING)
	@Column(name = "TYPE")
	private CustomisationType type; // 配置種類

	@Column(name = "NAME")
	private String name;

	@Column(name = "VALUE")
	private String value;

	@Column(name = "DESCRIPTION")
	private String description; // 敘述

	@Enumerated(EnumType.STRING)
	@Column(name = "ACTIVE_FLAG")
	private YesNo activeFlag; // 是否生效

	/**
	 * 建立個人化配置
	 * 
	 * @param command
	 */
	public void create(CreateCustomisationCommand command) {
		this.username = Objects.isNull(ContextHolder.getUsername()) ? ContextHolder.getUsername()
				: command.getUsername();
		this.type = CustomisationType.valueOf(command.getType());
		this.name = command.getName();
		this.value = command.getValue();
		this.description = command.getDescription();
		this.activeFlag = YesNo.Y;
	}

	/**
	 * 更新個人化配置
	 * 
	 * @param command
	 */
	public void update(UpdateCustomisationCommand command) {
		this.name = command.getName();
		this.value = command.getValue();
		this.description = command.getDescription();
		this.activeFlag = YesNo.valueOf(command.getActiveFlag());
	}

	/**
	 * 更新個人化設定的值
	 * 
	 * @param value
	 */
	public void update(String value) {
		this.value = value;
	}

	/**
	 * 新增個人化設定的值
	 * 
	 * @param username
	 * @param type
	 * @param name
	 * @param value
	 */
	public void create(String username, String type, String name, String value) {
		this.username = username;
		this.type = CustomisationType.valueOf(type);
		this.name = name;
		this.value = value;
		this.activeFlag = YesNo.Y;
	}

}
