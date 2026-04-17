package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.base.application.service.BaseApplicationService;
import com.example.demo.domain.service.TrainService;
import com.example.demo.domain.share.TrainDetailQueriedData;
import com.example.demo.domain.share.TrainQueriedData;
import com.example.demo.domain.share.TrainSummaryQueriedData;
import com.example.demo.domain.train.command.QueryTrainCommand;
import com.example.demo.domain.train.command.QueryTrainSummaryCommand;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class TrainQueryService extends BaseApplicationService {

	private TrainService trainService;

	/**
	 * 查詢火車資訊
	 * 
	 * @param trainNo 火車號次
	 * @return 火車資訊
	 */
	public TrainQueriedData queryTrainData(Integer trainNo) {
		return trainService.query(trainNo);
	}

	/**
	 * 透過條件查詢該火車資訊
	 * 
	 * @param command
	 * @return 火車資訊
	 */
	public List<TrainSummaryQueriedData> queryTrainSummary(QueryTrainSummaryCommand command) {
		return trainService.queryTrainSummary(command);
	}
	
	/**
	 * 透過條件查詢該火車資訊(供訂票查詢用)
	 * 
	 * @param command
	 * @return 火車資訊
	 */
	public List<TrainDetailQueriedData> queryTrainInfo(QueryTrainCommand command) {
		return trainService.queryTrainInfo(command);
	}
}
