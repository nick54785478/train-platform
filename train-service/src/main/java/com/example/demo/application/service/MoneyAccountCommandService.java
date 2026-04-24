package com.example.demo.application.service;

import java.util.List;
import java.util.Objects;

import org.springframework.context.annotation.Lazy;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.base.application.port.AuthServiceClientPort;
import com.example.demo.base.application.service.BaseApplicationService;
import com.example.demo.base.domain.aggregate.DomainEvent;
import com.example.demo.base.shared.exception.exception.ResourceNotFoundException;
import com.example.demo.base.shared.exception.exception.ValidationException;
import com.example.demo.domain.account.aggregate.MoneyAccount;
import com.example.demo.domain.account.command.ChargeFareCommand;
import com.example.demo.domain.account.command.CreateMoneyAccountCommand;
import com.example.demo.domain.account.command.DepositMoneyCommand;
import com.example.demo.domain.account.command.RegisterUserCommand;
import com.example.demo.domain.account.outbound.AccountDepositFailedEvent;
import com.example.demo.domain.account.outbound.FareChargedFailedEvent;
import com.example.demo.domain.booking.aggregate.TicketBooking;
import com.example.demo.domain.service.PaymentDomainService;
import com.example.demo.iface.dto.res.UserRegisteredResource;
import com.example.demo.infra.repository.MoneyAccountRepository;
import com.example.demo.infra.repository.TicketBookingRepository;
import com.example.demo.util.BaseDataTransformer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, timeout = 36000, rollbackFor = Exception.class)
public class MoneyAccountCommandService extends BaseApplicationService {

	private final AuthServiceClientPort authServiceClient;
	private final PaymentDomainService paymentService;
	private final MoneyAccountRepository moneyAccountRepository;
	private final TicketBookingRepository ticketBookingRepository;
	private final BookingCommandService self;

	public MoneyAccountCommandService(AuthServiceClientPort authServiceClient, PaymentDomainService paymentService,
			MoneyAccountRepository moneyAccountRepository, TicketBookingRepository ticketBookingRepository,
			@Lazy BookingCommandService self) {
		this.authServiceClient = authServiceClient;
		this.paymentService = paymentService;
		this.moneyAccountRepository = moneyAccountRepository;
		this.ticketBookingRepository = ticketBookingRepository;
		this.self = self;
	}

	/**
	 * 註冊儲值帳戶
	 * 
	 * @param command
	 * @return MoneyAccountRegisteredData
	 */
	public void register(CreateMoneyAccountCommand command) {

		MoneyAccount account = moneyAccountRepository.findByEmail(command.getEmail());
		if (!Objects.isNull(account)) {
			log.error("該信箱已被註冊");
			throw new ValidationException("VALIDATE_FAILED", "該信箱已被註冊");
		}

		// 建立 Money Account
		MoneyAccount moneyAccount = MoneyAccount.create(command);
		moneyAccountRepository.save(moneyAccount);

		// 取出 Domain Event 清單
		List<DomainEvent> domainEvents = moneyAccount.getDomainEvents();

		// 註冊 Event 到 Outbox
		this.saveDomainEventsToOutbox(domainEvents);

		// 呼叫外部 API，註冊使用者 (此處 try-catch，若已經註冊就算了，無需處理)
		try {
			RegisterUserCommand registerUserCommand = BaseDataTransformer.transformData(command,
					RegisterUserCommand.class);
			UserRegisteredResource response = authServiceClient.register(registerUserCommand);
			log.debug("code:{}, message:{}", response.getCode(), response.getMessage());
		} catch (Exception e) {
			log.warn("該帳號已註冊:{}", command.getUsername());
		}

	}

	@Transactional
	@Retryable(retryFor = { CannotAcquireLockException.class }, maxAttempts = 3)
	public void regularDeposit(DepositMoneyCommand command, String eventTxId) {
		moneyAccountRepository.findById(command.getUuid()).ifPresentOrElse(account -> {

			// 執行日常儲值邏輯
			account.regularDeposit(command.getMoney(), eventTxId);

			moneyAccountRepository.save(account);

			// 寫入 Outbox
			this.saveDomainEventsToOutbox(account.getDomainEvents());

			log.info("帳戶 {} 日常儲值成功，金額: {}", command.getUuid(), command.getMoney());

		}, () -> {
			throw new ResourceNotFoundException("ENTITY_NOT_FOUND", "找不到儲值帳戶: " + command.getUuid());
		});
	}

	/**
	 * 進行帳戶儲值(加錢)
	 * 
	 * @param command {@link DepositMoneyCommand}
	 * @return MoneyDepositedRegisteredData
	 */
	@Retryable(retryFor = { CannotAcquireLockException.class }, maxAttempts = 3, backoff = @Backoff(delay = 100))
	public void depositAndActivate(DepositMoneyCommand command, String eventTxId) {
		try {
			MoneyAccount account = moneyAccountRepository.findById(command.getUuid())
					.orElseThrow(() -> new RuntimeException("Account not found"));

			// 1. 執行加錢邏輯
			account.deposit(command.getMoney());

			// 2. 激活帳戶並註冊 AccountRegistrationCompletedEvent
			account.activate(eventTxId);

			// 3. 持久化
			moneyAccountRepository.save(account);

			// 4. 【關鍵】成功路徑：將事件寫入 Outbox (隨主交易 Commit)
			// 這樣 AccountSagaCompletionEventHandler 才會接到通知
			this.saveDomainEventsToOutbox(account.getDomainEvents());

			log.info("帳戶 {} 初始儲值與激活成功，成功路徑終點事件已寫入 Outbox", command.getUuid());
		} catch (Exception e) {
			log.error("初始儲值失敗，準備發送凍結補償事件: {}", e.getMessage());

			// 建立補償事件
			AccountDepositFailedEvent failEvent = AccountDepositFailedEvent.builder().targetId(command.getUuid())
					.money(command.getMoney()).reason(e.getMessage()).eventTxId(eventTxId).build();

			System.out.println("failEvent:" + failEvent.getEventTxId());

			// 強制寫入 Outbox (使用 REQUIRES_NEW 確保不會被 Rollback)
			self.forceSaveDomainEvent(failEvent);
			throw e; // 拋出異常回滾儲值動作
		}
	}

	/**
	 * 執行帳戶凍結 (補償動作)
	 */
	@Retryable(retryFor = { CannotAcquireLockException.class }, maxAttempts = 3, backoff = @Backoff(delay = 100))
	public void freezeAccount(String accountUuid, String reason, String sagaEventTxId) {
		moneyAccountRepository.findById(accountUuid).ifPresent(account -> {
			// 1. 執行凍結並註冊 Domain Event
			account.freeze(reason, sagaEventTxId);

			// 2. 持久化狀態
			moneyAccountRepository.save(account);

			// 3. 關鍵：將 Domain Events 存入 Outbox 資料表
			// 這樣 AccountRegistrationFailedEvent 才會被發送到 RabbitMQ
			this.saveDomainEventsToOutbox(account.getDomainEvents());

			log.info("帳戶 {} 已凍結，終點事件已寫入 Outbox", accountUuid);
		});
	}

	/**
	 * 進行扣款並更新訂單狀態
	 * 
	 * @param command   {@link ChargeFareCommand}
	 * @param eventTxId 事件交易唯一值
	 */
	@Transactional
	@Retryable(retryFor = { CannotAcquireLockException.class }, maxAttempts = 3, backoff = @Backoff(delay = 100))
	public void charge(ChargeFareCommand command, String eventTxId) {

		try {
			// 1. 透過 Repository 載入聚合根 (Infrastructure)
			TicketBooking booking = ticketBookingRepository.findById(command.getBookingUuid()).orElseThrow();
			MoneyAccount account = moneyAccountRepository.findById(command.getUuid()).orElseThrow();

			// 2. 委派給 Domain Service 執行核心業務 (Domain Logic)
			paymentService.executePayment(booking, account, command.getMoney(), eventTxId);

			// 3. 持久化變更 (Infrastructure)
			moneyAccountRepository.save(account);
			ticketBookingRepository.save(booking);

			// 4. 發送事件 (Infrastructure / Outbox)
			this.saveDomainEventsToOutbox(account.getDomainEvents());

		} catch (Exception e) {
			log.error("扣款失敗，原因: {}，觸發補償機制", e.getMessage());

			// 建立補償事件
			FareChargedFailedEvent failedEvent = FareChargedFailedEvent.builder().accountUuid(command.getUuid())
					.targetId(command.getBookingUuid()).eventTxId(eventTxId).reason(e.getMessage()).build();

			// 這裡要特別小心：
			// 如果你拋出異常 (throw e)，@Transactional 會回滾。
			// 這會導致你剛剛存進 Outbox 的 failedEvent 也跟著被回滾，結果訊息發不出去！

			// 發送失敗事件給 Saga 協調器或相關 Handler
			self.forceSaveDomainEvent(failedEvent);
			throw e;
		}
	}

}
