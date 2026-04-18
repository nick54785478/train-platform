package com.example.demo.domain.account.aggregate;

import java.math.BigDecimal;
import java.util.UUID;

import com.example.demo.base.domain.aggregate.BaseAggreagteRoot;
import com.example.demo.domain.account.aggregate.vo.AccountStatus;
import com.example.demo.domain.account.command.CreateMoneyAccountCommand;
import com.example.demo.domain.account.outbound.AccountRegisteredEvent;
import com.example.demo.domain.account.outbound.AccountRegistrationCompletedEvent;
import com.example.demo.domain.account.outbound.AccountRegistrationFailedEvent;
import com.example.demo.domain.account.outbound.FareChargedEvent;
import com.example.demo.domain.account.outbound.MoneyDepositedEvent;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "MONEY_ACCOUNT")
public class MoneyAccount extends BaseAggreagteRoot {

	@Id
	@Column(name = "UUID")
	private String uuid;

	@Column(name = "NAME")
	private String name; // 人名

	@Column(name = "USERNAME")
	private String username; // 帳號

	@Column(name = "EMAIL")
	private String email; // email

	@Column(name = "BALANCE")
	private BigDecimal balance = new BigDecimal("0"); // 餘額

	@Enumerated(EnumType.STRING)
	@Column(name = "ACCOUNT_STATUS")
	private AccountStatus status; // 帳戶狀態

	/**
	 * 新增帳戶訊息
	 * 
	 * @param command {@link CreateMoneyAccountCommand }
	 * @return MoneyAccount
	 */
	public static MoneyAccount create(CreateMoneyAccountCommand command) {
		MoneyAccount moneyAccount = new MoneyAccount();

		// 1. 在 Saga 起點顯式生成一個交易 ID
		String sagaTxId = UUID.randomUUID().toString();

		moneyAccount.uuid = UUID.randomUUID().toString();
		moneyAccount.username = command.getUsername();
		moneyAccount.name = command.getName();
		moneyAccount.email = command.getEmail();
		moneyAccount.status = AccountStatus.PENDING;

		// 2. 賦予實體這個 ID
		moneyAccount.assignEventTxId(sagaTxId);

		// 3. 建立事件時，必須明確傳入這個 sagaTxId
		AccountRegisteredEvent event = AccountRegisteredEvent.builder().targetId(moneyAccount.uuid).eventTxId(sagaTxId)
				.money(command.getMoney()).email(moneyAccount.email).build();

		moneyAccount.raiseEvent(event);
		return moneyAccount;
	}

	/**
	 * 激活帳戶 (儲值成功後呼叫)
	 */
	public void activate(String sagaEventTxId) {
		// 業務規則檢查
		if (this.status != AccountStatus.PENDING && this.status != AccountStatus.FROZEN) {
			throw new IllegalStateException("只有 PENDING 或 FROZEN 狀態的帳戶可以被激活");
		}
		this.status = AccountStatus.ACTIVE;

		// 建立「成功終點」事件
		AccountRegistrationCompletedEvent successEvent = AccountRegistrationCompletedEvent.builder().targetId(this.uuid)
				.eventTxId(sagaEventTxId).username(this.username).email(this.email).build();
		this.raiseEvent(successEvent);
	}

	/**
	 * 凍結帳戶 (儲值失敗後呼叫)
	 */
	public void freeze(String reason, String sagaEventTxId) {
		this.status = AccountStatus.FROZEN;
		log.warn("帳戶 {} 已被凍結，原因: {}", this.uuid, reason);

		// 建立終點失敗事件 (這會被寫入 Outbox 並路由至 failure 隊列)
		AccountRegistrationFailedEvent finalEvent = AccountRegistrationFailedEvent.builder().targetId(this.uuid)
				.eventTxId(sagaEventTxId) // 沿用原本的 Saga 交易 ID
				.reason(reason).email(this.email) // 帶上 Email 方便 Handler 寄信
				.build();

		// 檢查 Builder 出來後是不是對的
		log.debug("DEBUG - Builder 剛建立的 ID: {}", finalEvent.getEventTxId());

		this.raiseEvent(finalEvent);

		// 檢查 raiseEvent 之後，ID 有沒有被 Entity 偷換掉
		log.debug("DEBUG - raiseEvent 之後的 ID: {}", finalEvent.getEventTxId());
	}

	/**
	 * 日常儲值
	 * 
	 * @param amount    金額
	 * @param eventTxId 該業務交易唯一值
	 */
	public void regularDeposit(BigDecimal amount, String eventTxId) {
		// 1. 狀態檢查：只有 ACTIVE 的帳號可以進行日常儲值
		if (this.status != AccountStatus.ACTIVE) {
			throw new IllegalStateException("帳戶目前處於 " + this.status + " 狀態，無法進行儲值操作。");
		}

		// 2. 金額檢查
		if (amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("儲值金額必須大於 0");
		}

		// 3. 執行儲值動作
		this.deposit(amount);

		// 4. 註冊事件
		MoneyDepositedEvent event = MoneyDepositedEvent.builder().targetId(this.uuid).eventTxId(eventTxId)
				.amount(amount).balanceAfter(this.balance) // 假設你有 balance 欄位
				.build();

		this.raiseEvent(event);
	}

	/**
	 * 儲值
	 * 
	 * @param amount 金額
	 */
	public void deposit(BigDecimal amount) {
		this.balance = this.balance.add(amount);
	}

	/**
	 * 付錢
	 * 
	 * @param amount 金額
	 */
	public void charge(BigDecimal amount, String bookingUuid, String eventTxId) {
		this.balance = this.balance.subtract(amount);

		// 賦予 Event Transaction ID 標註此次事件在同一次業務行為
		this.assignEventTxId(eventTxId);
		// 發布扣款事件
		FareChargedEvent event = FareChargedEvent.builder().targetId(bookingUuid).build();

		// 設置 Domain Event
		this.raiseEvent(event);
	}
}
