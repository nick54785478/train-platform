package com.example.demo.service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.application.shared.dto.TrainQueriedData;
import com.example.demo.application.shared.dto.TrainQueriedData.StopQueriedData;
import com.example.demo.base.application.service.BaseApplicationService;
import com.example.demo.base.shared.exception.exception.ValidationException;
import com.example.demo.domain.service.TrainService;
import com.example.demo.domain.share.dto.SummariedTrainGottenView;
import com.example.demo.domain.share.dto.TrainDetailGottenView;
import com.example.demo.domain.train.aggregate.Train;
import com.example.demo.domain.train.query.GetTrainQuery;
import com.example.demo.domain.train.query.SummaryTrainQuery;
import com.example.demo.infra.repository.TrainRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class TrainQueryService extends BaseApplicationService {

	private TrainService trainService;
	private TrainRepository trainRepository;

	/**
	 * 查詢火車資訊
	 * 
	 * @param trainNo 火車號次
	 * @return 火車資訊
	 */
	public TrainQueriedData queryTrainData(Integer trainNo) {
		Train train = trainRepository.findByNumber(trainNo);
		if (Objects.isNull(train)) {
			throw new ValidationException("VALIDATE_FAILED", "查無此車次 " + trainNo);
		}
		TrainQueriedData queriedData = this.transformData(train, TrainQueriedData.class);
		queriedData.getStops().sort(Comparator.comparingInt(StopQueriedData::getSeq));
		return queriedData;
	}

	/**
	 * 透過條件查詢該火車資訊
	 * 
	 * @param query {@link SummaryTrainQuery}
	 * @return 火車資訊
	 */
	public List<SummariedTrainGottenView> summary(SummaryTrainQuery query) {
		return trainService.summary(query);
	}

	/**
	 * 透過條件查詢該火車資訊(供訂票查詢用)
	 * 
	 * @param query {@link GetTrainQuery}
	 * @return 火車資訊
	 */
	public List<TrainDetailGottenView> queryTrainInfo(GetTrainQuery query) {
		return trainService.queryTrainInfo(query);
	}
}
