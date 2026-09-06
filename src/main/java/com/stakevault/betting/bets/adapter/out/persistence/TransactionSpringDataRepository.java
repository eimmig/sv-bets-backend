package com.stakevault.betting.bets.adapter.out.persistence;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface TransactionSpringDataRepository extends JpaRepository<TransactionJpaEntity, UUID> {

	// from/to comparados via COALESCE(:param, coluna), nao "(:param IS NULL OR ...)" - Postgres
	// nao consegue inferir o tipo de um parametro so usado num "IS NULL" isolado pra coluna
	// timestamp (achado real, ver BetSpringDataRepository.findFiltered/docs/CONVENTIONS.md).
	@Query("SELECT t FROM TransactionJpaEntity t WHERE "
			+ "(:bettingHouseId IS NULL OR t.bettingHouseId = :bettingHouseId) AND "
			+ "t.createdAt >= COALESCE(:from, t.createdAt) AND "
			+ "t.createdAt <= COALESCE(:to, t.createdAt) "
			+ "ORDER BY t.createdAt DESC")
	Page<TransactionJpaEntity> findFiltered(@Param("bettingHouseId") UUID bettingHouseId, @Param("from") Instant from,
			@Param("to") Instant to, Pageable pageable);

	@Query("SELECT t.bettingHouseId, SUM(CASE WHEN t.type = com.stakevault.betting.bets.domain.model.TransactionType.DEPOSIT "
			+ "THEN t.amount ELSE -t.amount END) FROM TransactionJpaEntity t "
			+ "WHERE t.bettingHouseId IN :bettingHouseIds GROUP BY t.bettingHouseId")
	List<Object[]> sumNetAmountByBettingHouseIds(@Param("bettingHouseIds") Collection<UUID> bettingHouseIds);
}
