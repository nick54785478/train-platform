package com.example.demo.iface.rest;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.domain.ticket.command.CreateOrUpdateTicketCommand;
import com.example.demo.domain.ticket.command.CreateTicketCommand;
import com.example.demo.iface.dto.req.CreateOrUpdateTicketResource;
import com.example.demo.iface.dto.req.CreateTicketResource;
import com.example.demo.iface.dto.res.TicketCreatedResource;
import com.example.demo.iface.dto.res.TicketQueriedResource;
import com.example.demo.service.TicketCommandService;
import com.example.demo.service.TicketQueryService;
import com.example.demo.util.BaseDataTransformer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/ticket")
@Tag(name = "Ticket API", description = "進行與車票領域相關動作")
public class TicketController {

	private TicketQueryService ticketQueryService;
	private TicketCommandService ticketCommandService;

	/**
	 * 建立車票資料
	 * 
	 * @param resource
	 * @return ResponseEntity
	 */
	@PostMapping("")
	@Operation(summary = "API - 建立車票資料", description = "建立車票資料。")
	public ResponseEntity<TicketCreatedResource> createTicket(
			@Parameter(description = "車票資訊") @RequestBody CreateTicketResource resource) {
		CreateTicketCommand command = BaseDataTransformer.transformData(resource, CreateTicketCommand.class);
		ticketCommandService.createTicket(command);
		return new ResponseEntity<>(new TicketCreatedResource("201", "新增/更新成功"), HttpStatus.OK);
	}

	/**
	 * 針對某車次批次建立車票資料
	 * 
	 * @param resource
	 * @return ResponseEntity<TicketCreatedResource>
	 */
	@PostMapping("/{trainNo}")
	@Operation(summary = "API - 針對某車次批次建立車票資料", description = "針對某車次批次建立車票資料。")
	public ResponseEntity<TicketCreatedResource> createOrUpdateTickets(
			@Parameter(description = "車次") @PathVariable Integer trainNo,
			@Parameter(description = "車票資訊") @RequestBody List<CreateOrUpdateTicketResource> resources) {
		// 將 train No 設置進該resource
		resources.stream().forEach(e -> e.setTrainNo(trainNo));
		List<CreateOrUpdateTicketCommand> commands = BaseDataTransformer.transformData(resources,
				CreateOrUpdateTicketCommand.class);
		ticketCommandService.createOrUpdateTickets(trainNo, commands);
		return new ResponseEntity<>(new TicketCreatedResource("200", "新增/更新成功"), HttpStatus.OK);
	}

	/**
	 * 查詢某車次批次的車票資料
	 * 
	 * @param trainNo 車次
	 * @return ResponseEntity<List<TicketQueriedResource>>
	 */
	@GetMapping("/{trainNo}")
	@Operation(summary = "API - 查詢某車次批次的車票資料", description = "查詢某車次批次的車票資料。")
	public ResponseEntity<List<TicketQueriedResource>> queryTicketsByTrainNo(
			@Parameter(description = "車次") @PathVariable Integer trainNo) {
		return new ResponseEntity<>(BaseDataTransformer.transformData(ticketQueryService.queryTicketsByTrainNo(trainNo),
				TicketQueriedResource.class), HttpStatus.OK);
	}

}
