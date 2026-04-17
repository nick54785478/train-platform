package com.example.demo.domain.train.aggregate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.example.demo.base.domain.aggregate.BaseAggreagteRoot;
import com.example.demo.domain.train.aggregate.entity.TrainStop;
import com.example.demo.domain.train.aggregate.vo.TrainKind;
import com.example.demo.domain.train.command.CreateStopCommand;
import com.example.demo.domain.train.command.CreateTrainCommand;
import com.example.demo.domain.train.command.UpdateTrainCommand;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Entity
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "TRAIN")
public class Train extends BaseAggreagteRoot {

	@Id
	@Column(name = "UUID")
	private String uuid;

	@Transient
	private String u;

	@Column(name = "TRAIN_NO")
	private Integer number;

	@Column(name = "TRAIN_KIND")
	@Enumerated(EnumType.STRING)
	private TrainKind kind;

	// 使用懶加載，避免 N+1 query 效能問題
	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, orphanRemoval = true)
	@JoinColumn(name = "TRAIN_UUID", updatable = false)
	private List<TrainStop> stops = new ArrayList<>();

	/**
	 * 在持久化之前執行的方法，用於設置 Train UUID。
	 */
	@PrePersist
	public void prePersist() {
		if (Objects.isNull(this.uuid)) {
			this.uuid = this.u;
			// 將 Train UUID 設置進 Stop
			for (TrainStop stop : stops) {
				stop.setTrainUuid(this.uuid);
			}
		}
	}

	/**
	 * Command Handler 用途: 新增車次
	 * 
	 * @param command
	 */
	public void create(CreateTrainCommand command) {
		this.u = UUID.randomUUID().toString();
		this.number = command.getTrainNo();
		this.kind = TrainKind.fromLabel(command.getTrainKind());
		// 將 Stop 資訊進行設置
		for (CreateStopCommand stop : command.getStops()) {
			TrainStop trainStop = new TrainStop();
			trainStop.create("", stop.getSeq(), stop.getStopName(), stop.getStopTime());
			stops.add(trainStop);
		}
	}

	/**
	 * Command Handler 用途: 更新車次資料
	 * 
	 * @param command
	 */
	public void update(UpdateTrainCommand command, String trainUuid) {
		this.stops.clear();
		List<String> uuids = this.stops.stream().map(TrainStop::getUuid).collect(Collectors.toList());
		// 移除 functions 中不存在於 Role Functions 的項目
		this.stops.removeIf(existingStop -> command.getStops().stream()
				.noneMatch(newStop -> StringUtils.equals(newStop.getUuid(), existingStop.getUuid())));
		command.getStops().stream().forEach(newStop -> {
			if (!uuids.contains(newStop.getUuid())) {
				TrainStop trainStop = new TrainStop();
				trainStop.create(trainUuid, newStop.getSeq(), newStop.getStopName(), newStop.getStopTime());
				this.stops.add(trainStop);
			}
		});
	}

	/**
	 * 更新 Stops
	 * */
	public void updateStops(List<TrainStop> stops) {
		this.stops = stops;
	}

}
