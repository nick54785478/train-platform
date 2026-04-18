package com.example.demo.domain.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.demo.base.domain.service.BaseDomainService;
import com.example.demo.base.shared.exception.exception.ValidationException;
import com.example.demo.domain.shared.dto.TicketQueriedView;
import com.example.demo.domain.ticket.aggregate.Ticket;
import com.example.demo.domain.ticket.command.CreateOrUpdateTicketCommand;
import com.example.demo.domain.ticket.command.CreateTicketCommand;
import com.example.demo.domain.train.aggregate.Train;
import com.example.demo.infra.repository.TicketRepository;
import com.example.demo.infra.repository.TrainRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Domain Service
 */
@Slf4j
@Service
@AllArgsConstructor
public class TicketService extends BaseDomainService {

	private TrainRepository trainRepository;
	private TicketRepository ticketRepository;

	/**
	 * 新增車票資料
	 * 
	 * @param command
	 */
	public void create(CreateTicketCommand command) {
		Train train = trainRepository.findByNumber(command.getTrainNo());

		// 領域檢核 檢查車次是否存在(車次存在才可以新增)
		if (Objects.isNull(train)) {
			log.error("該車次不存在");
			throw new ValidationException("VALIDATE_FAILED", "該車次不存在");
		}
		Ticket ticket = new Ticket();
		ticket.create(command, train.getUuid());
		ticketRepository.save(ticket); // 將ticket資料存入資料庫
	}

	/**
	 * 批次新增車票資料
	 * 
	 * @param command
	 */
	public void create(List<CreateTicketCommand> commands) {
		Optional<CreateTicketCommand> any = commands.stream().findAny();
		if (any.isPresent()) {
			// 車次
			Integer trainNo = any.get().getTrainNo();
			Train train = trainRepository.findByNumber(trainNo);
			List<Ticket> tickets = commands.stream().map(command -> {
				Ticket ticket = new Ticket();
				ticket.create(command, train.getUuid());
				return ticket;
			}).collect(Collectors.toList());
			ticketRepository.saveAll(tickets); // 將 ticket 資料存入資料庫
		} else {
			log.error( "該車次不存在");
			throw new ValidationException("VALIDATE_FAILED", "該車次不存在");
		}
	}

	/**
	 * 批次新增車票資料
	 * 
	 * @param trainNo
	 * @param commands
	 */
	public void createOrUpdate(Integer trainNo, List<CreateOrUpdateTicketCommand> commands) {
		Train train = trainRepository.findByNumber(trainNo);

		// 領域檢核 檢查車次是否存在(車次存在才可以新增)
		if (Objects.isNull(train)) {
			throw new ValidationException("VALIDATE_FAILED", "該車次不存在");
		}

		// 查出所有車票資料
		List<Ticket> ticketList = ticketRepository.findByTrainUuid(train.getUuid());

		// 轉換 commands 成 Map<TicketNo, command>
		Map<String, CreateOrUpdateTicketCommand> commandMap = commands.stream()
				.collect(Collectors.toMap(cmd -> cmd.getTicketNo(), Function.identity(), (a, b) -> a));

		// 轉換 ticketList 成 Map<TicketNo, Ticket>
		Map<String, Ticket> ticketMap = ticketList.stream()
				.collect(Collectors.toMap(Ticket::getTicketNo, Function.identity()));

		// 刪除: 舊資料有但新資料沒有
		List<Ticket> toDelete = ticketList.stream().filter(ticket -> !commandMap.containsKey(ticket.getTicketNo())) // 舊資料不在新資料內
				.toList();
		ticketRepository.deleteAll(toDelete); // 批次刪除

		// 更新: 兩者都有，檢查是否變更
		List<Ticket> toUpdate = ticketList.stream().filter(ticket -> commandMap.containsKey(ticket.getTicketNo())) // 兩者都有
				.map(ticket -> {
					CreateOrUpdateTicketCommand cmd = commandMap.get(ticket.getTicketNo());
					// 更新 Ticket 內容
					ticket.update(cmd, train.getUuid());
					return ticket;
				}).toList();
		ticketRepository.saveAll(toUpdate); // 批次更新

		// 5. 新增: 新資料有但舊資料沒有
		List<Ticket> toCreate = commands.stream().filter(cmd -> !ticketMap.containsKey(cmd.getTicketNo())) // 只有新資料有
				.map(cmd -> {
					Ticket ticket = new Ticket();
					ticket.create(cmd, train.getUuid());
					return ticket;
				}).toList();
		ticketRepository.saveAll(toCreate); // 批次新增
	}

	/**
	 * 根據車次查詢車票資料
	 * 
	 * @param trainNo
	 * @return List<TicketQueriedData>
	 */
	public List<TicketQueriedView> queryTicketsByTrainNo(Integer trainNo) {
		// 查出車次資料 => trainUuid
		Train train = trainRepository.findByNumber(trainNo);
		// 領域檢核 檢查車次是否存在(車次存在才可以新增)
		if (Objects.isNull(train)) {
			throw new ValidationException("VALIDATE_FAILED", "該車次不存在");
		}
		List<Ticket> tickets = ticketRepository.findByTrainUuid(train.getUuid());
		return this.transformAggregate(tickets, TicketQueriedView.class);
	}

}
