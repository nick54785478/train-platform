package com.example.demo.domain.train.aggregate.entity;

import java.math.BigDecimal;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Entity
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TRAIN_TICKET_V")
public class TrainTicketV {

	@Id
	private String id;
	
	@Column(name = "TRAIN_UUID")
	private String trainUuid;

	@Column(name = "TRAIN_NO")
	private Integer trainNo;
	
	@Column(name = "TRAIN_KIND")
	private String trainKind;

//	@Column(name = "TICKET_UUID")
//	private Integer ticketUuid;

	@Column(name = "FROM_STOP")
	private String fromStop; // 起站

	@Column(name = "TO_STOP")
	private String toStop; // 迄站

	@Column(name = "PRICE")
	private BigDecimal price; // 票價

	@Column(name = "STOP_NAME")
	private String stopName; // 站名

	@Column(columnDefinition = "ARRIVE_TIME")
	private LocalTime arriveTime; // 停站時間

	@Column(name = "SEQ")
	private Integer seq; // 停站順序

}
