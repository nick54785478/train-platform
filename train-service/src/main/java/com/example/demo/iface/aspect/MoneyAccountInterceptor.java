package com.example.demo.iface.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.springframework.stereotype.Component;

import com.example.demo.base.infra.context.ContextHolder;
import com.example.demo.base.shared.event.BaseEvent;
import com.example.demo.domain.account.aggregate.MoneyAccount;
import com.example.demo.domain.service.MoneyAccountService;
import com.example.demo.util.JsonParseUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 針對 MoneyAccount 領域的攔截器，用於紀錄 Event Source
 */
@Slf4j
//@Aspect
@Component
public class MoneyAccountInterceptor {

	private MoneyAccountService moneyAccountService;

	public MoneyAccountInterceptor(MoneyAccountService moneyAccountService) {
		this.moneyAccountService = moneyAccountService;
	}

	/**
	 * 定義切入點，針對 MoneyAccountRepository 的 save 方法進行切入。
	 */
//	@Pointcut("execution(* com.example.demo.infra.repository.MoneyAccountRepository.save(..))")
	public void pointCut() {

	}

	/**
	 * 對 MoneyAccount 領域資料 執行 EventSourcing
	 * 
	 * @param joinPoint 切入點
	 * @return 方法執行結果
	 * @throws Throwable 例外
	 */
	@Around("pointCut()")
	public Object executeEventSourcing(ProceedingJoinPoint joinPoint) throws Throwable {
		Object[] args = joinPoint.getArgs();

		MoneyAccount entity = (MoneyAccount) args[0]; // 獲取 MoneyAccount
		log.info("[execute EventSourcing] Entity -> uuid:{}, username:{}, email:{}, balance:{}", entity.getUuid(),
				entity.getUsername(), entity.getEmail(), entity.getBalance());

		BaseEvent event = ContextHolder.getEvent();
		log.info("eventLog: {}, targetId:{}", event.getEventLogUuid(), event.getTargetId());

		// 紀錄 EventSource
		String eventStreamId = entity.getClass().getSimpleName() + "-" + entity.getUuid();
		moneyAccountService.addEventSource(eventStreamId, JsonParseUtil.serialize(entity));

		// 執行後續流程
		return joinPoint.proceed();

	}

}
