package com.example.demo.infra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.domain.account.aggregate.MoneyAccount;

@Repository
public interface MoneyAccountRepository extends JpaRepository<MoneyAccount, String> {

	MoneyAccount findByEmail(String email);

	MoneyAccount findByUsername(String username);

}
