package com.example.demo.domain.template.aggregate;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.example.demo.base.domain.aggregate.BaseAggreagteRoot;
import com.example.demo.base.shared.enums.YesNo;
import com.example.demo.domain.template.aggregate.vo.FileType;
import com.example.demo.domain.template.aggregate.vo.TemplateType;
import com.example.demo.domain.template.command.UploadTemplateCommand;

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

/**
 * 範本設定
 */
@Entity
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TEMPLATE")
@EntityListeners(AuditingEntityListener.class)
public class Template extends BaseAggreagteRoot {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "NAME")
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(name = "TEMPLATE_TYPE")
	private TemplateType type; // 範本種類

	@Enumerated(EnumType.STRING)
	@Column(name = "FILE_TYPE")
	private FileType fileType; // 檔案種類

	@Column(name = "FILE_PATH")
	private String filePath; // 檔案路徑

	@Column(name = "FILE_NAME")
	private String fileName; // 檔案名稱

	@Enumerated(EnumType.STRING)
	@Column(name = "DELETE_FLAG")
	private YesNo deleteFlag; // 是否刪除

	/**
	 * 建立 Template 資料
	 * 
	 * @param command
	 */
	public void create(UploadTemplateCommand command) {
		this.name = command.getName();
		this.type = TemplateType.valueOf(command.getType());
		this.fileType = FileType.fromLabel(command.getFileType());
		this.filePath = command.getFilePath();
		this.fileName = command.getFileName();
		this.deleteFlag = YesNo.N;
	}

	/**
	 * 更新 Template 資料
	 * 
	 * @param command
	 */
	public void update(UploadTemplateCommand command) {
		this.filePath = command.getFileType();
		this.fileName = command.getFileName();
	}

}
