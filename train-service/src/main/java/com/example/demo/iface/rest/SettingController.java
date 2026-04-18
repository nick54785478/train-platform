package com.example.demo.iface.rest;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.application.service.SettingCommandService;
import com.example.demo.application.service.SettingQueryService;
import com.example.demo.domain.setting.command.CreateSettingCommand;
import com.example.demo.domain.setting.command.UpdateSettingCommand;
import com.example.demo.iface.dto.req.CreateSettingResource;
import com.example.demo.iface.dto.req.UpdateSettingResource;
import com.example.demo.iface.dto.res.SettingCreatedResource;
import com.example.demo.iface.dto.res.SettingDeletedResource;
import com.example.demo.iface.dto.res.SettingQueriedResource;
import com.example.demo.iface.dto.res.SettingUpdatedResource;
import com.example.demo.util.BaseDataTransformer;

import lombok.AllArgsConstructor;

/**
 * Setting API
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/settings")
public class SettingController {

	private SettingQueryService settingQueryService;
	private SettingCommandService settingCommandService;

	/**
	 * 新增 設定
	 * 
	 * @param resource
	 * @return ResponseEntity<UserCreatedResource>
	 */
	@PostMapping("")
	public ResponseEntity<SettingCreatedResource> create(@RequestBody CreateSettingResource resource) {
		// 防腐處理 resource -> command
		CreateSettingCommand command = BaseDataTransformer.transformData(resource, CreateSettingCommand.class);
		settingCommandService.create(command);
		return new ResponseEntity<>(new SettingCreatedResource("201", "成功新增一筆資料"), HttpStatus.CREATED);
	}

	/**
	 * 查詢設定
	 * 
	 * @param dataType
	 * @param type
	 * @param name
	 * @param activeFlag
	 * @return ResponseEntity<List<SettingQueriedResource>>
	 */
	@GetMapping("/query")
	public ResponseEntity<List<SettingQueriedResource>> query(@RequestParam(required = false) String dataType,
			@RequestParam(required = false) String type, @RequestParam(required = false) String name,
			@RequestParam(required = false) String activeFlag) {
		return new ResponseEntity<>(
				BaseDataTransformer.transformData(settingQueryService.query(dataType, type, name, activeFlag),
						SettingQueriedResource.class),
				HttpStatus.OK);
	}

	/**
	 * 修改 設定
	 * 
	 * @param resource
	 * @return ResponseEntity<UserCreatedResource>
	 */
	@PutMapping("/{id}")
	public ResponseEntity<SettingUpdatedResource> update(@PathVariable Long id,
			@RequestBody UpdateSettingResource resource) {
		// 防腐處理 resource -> command
		UpdateSettingCommand command = BaseDataTransformer.transformData(resource, UpdateSettingCommand.class);
		command.setId(id);
		settingCommandService.update(command);
		return new ResponseEntity<>(new SettingUpdatedResource("200", "成功更新一筆資料"), HttpStatus.CREATED);
	}

	/**
	 * 刪除特定設定
	 * 
	 * @param resource
	 * @return ResponseEntity<SettingDeletedResource>
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<SettingDeletedResource> create(@PathVariable Long id) {
		settingCommandService.delete(id);
		return new ResponseEntity<>(new SettingDeletedResource("200", "成功刪除一筆資料"), HttpStatus.OK);
	}

}
