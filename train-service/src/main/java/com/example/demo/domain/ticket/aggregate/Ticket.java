package com.example.demo.domain.ticket.aggregate;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

import com.example.demo.base.domain.aggregate.BaseAggreagteRoot;
import com.example.demo.domain.ticket.command.CreateOrUpdateTicketCommand;
import com.example.demo.domain.ticket.command.CreateTicketCommand;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "TRAIN_TICKET")
public class Ticket extends BaseAggreagteRoot {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "TICKET_NO")
	private String ticketNo; // Aggregate Identifier

	@Column(name = "TRAIN_UUID")
	private String trainUuid;

	@Column(name = "FROM_STOP")
	private String fromStop; // 起站

	@Column(name = "TO_STOP")
	private String toStop; // 迄站

	@Column(name = "PRICE")
	private BigDecimal price; // 票價

	/**
	 * 在持久化之前執行的方法。
	 */
	@PrePersist
	public void prePersist() {
		// 新增時沒有 UUID，設置 UUID
		if (Objects.isNull(this.ticketNo)) {
			this.ticketNo = UUID.randomUUID().toString();
		}
	}

	/**
	 * 建立 票券資料
	 * 
	 * @param command
	 * @param trainUuid 火車uuid
	 */
	public void create(CreateTicketCommand command, String trainUuid) {
		this.price = command.getPrice();
		this.trainUuid = trainUuid;
		this.fromStop = command.getFromStop();
		this.toStop = command.getToStop();
	}

	/**
	 * 建立 票券資料
	 * 
	 * @param command
	 * @param trainUuid 火車 uuid
	 */
	public void create(CreateOrUpdateTicketCommand command, String trainUuid) {
		this.price = command.getPrice();
		this.trainUuid = trainUuid;
		this.fromStop = command.getFromStop();
		this.toStop = command.getToStop();
	}

	/**
	 * 更新 票券資料
	 * 
	 * @param command
	 * @param trainUuid 火車 uuid
	 */
	public void update(CreateOrUpdateTicketCommand command, String trainUuid) {
		this.price = command.getPrice();
		this.trainUuid = trainUuid;
		this.fromStop = command.getFromStop();
		this.toStop = command.getToStop();
	}

	/**
	 * 更新火車 uuid
	 * 
	 * @param trainUuid
	 */
	public void updateTrainUuid(String trainUuid) {
		this.trainUuid = trainUuid;
	}

}
