package com.stakevault.betting.bets.adapter.out.persistence;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface TransactionSpringDataRepository extends JpaRepository<TransactionJpaEntity, UUID> {

	Page<TransactionJpaEntity> findByBettingHouseId(UUID bettingHouseId, Pageable pageable);

	@Query("SELECT t.bettingHouseId, SUM(CASE WHEN t.type = com.stakevault.betting.bets.domain.model.TransactionType.DEPOSIT "
			+ "THEN t.amount ELSE -t.amount END) FROM TransactionJpaEntity t "
			+ "WHERE t.bettingHouseId IN :bettingHouseIds GROUP BY t.bettingHouseId")
	List<Object[]> sumNetAmountByBettingHouseIds(@Param("bettingHouseIds") Collection<UUID> bettingHouseIds);
}
