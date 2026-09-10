package com.stakevault.betting.bets.adapter.out.persistence;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface BettingHouseSpringDataRepository extends JpaRepository<BettingHouseJpaEntity, UUID> {

	boolean existsByName(String name);

	@Query("SELECT SUM(b.initialBalance) FROM BettingHouseJpaEntity b")
	BigDecimal sumInitialBalance();
}
