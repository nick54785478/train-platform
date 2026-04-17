package com.example.demo.domain.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.demo.base.shared.enums.YesNo;
import com.example.demo.domain.share.OptionQueriedData;
import com.example.demo.domain.share.enums.TicketType;
import com.example.demo.domain.train.aggregate.Train;
import com.example.demo.domain.train.aggregate.vo.TrainKind;
import com.example.demo.infra.repository.SettingRepository;
import com.example.demo.infra.repository.TrainRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class OptionService {

	private SettingRepository settingRepository;
	private TrainRepository trainRepository;

	/**
	 * 查詢相關的設定 (dropdown)
	 * 
	 * @param dataType 設定種類
	 * @return List<OptionQueried>
	 */
	public List<OptionQueriedData> getSettingsByDataType(String dataType) {
		return settingRepository.findByDataTypeAndActiveFlag(dataType, YesNo.Y).stream().map(setting -> {
			return new OptionQueriedData(setting.getId(), setting.getType(), setting.getType());
		}).collect(Collectors.toList());
	}

	/**
	 * 取得火車類別
	 * 
	 * @return List<OptionQueried>
	 */
	public List<OptionQueriedData> getTrainKinds() {
		Map<String, TrainKind> kindMap = TrainKind.getMap();
		return kindMap.values().stream().map(v -> new OptionQueriedData(null, v.getCode(), v.getLabel()))
				.collect(Collectors.toList());
	}

	/**
	 * 取得車票種類
	 * 
	 * @return List<OptionQueried>
	 */
	public List<OptionQueriedData> getTicketTypes() {
		Map<String, TicketType> kindMap = TicketType.getMap();
		return kindMap.values().stream().map(v -> new OptionQueriedData(null, v.getLabel(), v.getLabel()))
				.collect(Collectors.toList());
	}

	/**
	 * 取得火車車次資料 (下拉式選單)
	 */
	public List<OptionQueriedData> getTrainNoList() {
		return trainRepository.findAll().stream().map(Train::getNumber).distinct().map(e -> {
			return new OptionQueriedData(null, String.valueOf(e), String.valueOf(e));
		}).distinct().collect(Collectors.toList());

	}

	/**
	 * 取得特定設定資料
	 * 
	 * @param dataType
	 * @param type
	 * @return List<OptionQueriedData>
	 */
	public List<OptionQueriedData> getSettingsByDataTypeAndType(String dataType, String type) {
		return settingRepository.findByDataTypeAndTypeAndActiveFlag(dataType, type, YesNo.Y).stream().map(setting -> {
			return new OptionQueriedData(null, setting.getName(), setting.getValue());
		}).collect(Collectors.toList());
	}
}
