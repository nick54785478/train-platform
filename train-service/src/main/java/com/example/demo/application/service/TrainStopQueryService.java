package com.example.demo.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.domain.service.TrainStopService;
import com.example.demo.domain.shared.dto.StopDetailQueriedView;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class TrainStopQueryService {

	private TrainStopService trainStopService;
	
	/**
	 * 查詢該車次停靠站的詳細資訊(含各停靠站及其票價)
	 * 
	 * @param uuid     火車唯一代碼
	 * @param fromStop 起站
	 */
	public List<StopDetailQueriedView> getStopDetails(String uuid, String fromStop) {
		return trainStopService.getStopDetails(uuid, fromStop);
	}
}
