package com.example.demo.base.infra.adapter;

import java.time.LocalDateTime;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.base.application.port.BusinessIdempotenceExecutorPort;
import com.example.demo.base.infra.persistence.BusinessIdempotenceRepository;
import com.example.demo.base.shared.command.ExecuteIdempotenceCommand;
import com.example.demo.base.shared.entity.BusinessIdempotenceLog;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BusinessIdempotenceExecutorAdapter implements BusinessIdempotenceExecutorPort {

	private final BusinessIdempotenceRepository repository;

	/**
	 * 執行業務冪等校驗 利用 MySQL Unique Constraint 達成分散式環境下的資源爭搶控制
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW) // 強制開啟新事務，確保鎖定結果能立刻被其他執行緒看見
	public boolean execute(ExecuteIdempotenceCommand command) {
		try {
			// 嘗試寫入一筆權限紀錄
			BusinessIdempotenceLog logEntry = BusinessIdempotenceLog.builder().businessKey(command.getEventLogUuid())
					.targetId(command.getTargetId()).createdAt(LocalDateTime.now()).build();

			repository.saveAndFlush(logEntry);
			return true; // 寫入成功，代表拿到執行權

		} catch (DataIntegrityViolationException e) {
			// 捕捉唯一鍵衝突異常 (Duplicate Key)
			log.warn("業務冪等攔截：Key {} 已存在，執行權獲取失敗", command.getEventLogUuid());
			return false; // 寫入失敗，代表已被他人搶先
		} catch (Exception e) {
			log.error("業務冪等校驗發生未知異常: {}", e.getMessage());
			return false;
		}
	}

}