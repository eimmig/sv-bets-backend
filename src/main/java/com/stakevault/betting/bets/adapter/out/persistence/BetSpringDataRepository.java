package com.stakevault.betting.bets.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.stakevault.betting.bets.domain.model.BetFilter;
import com.stakevault.betting.bets.domain.model.BetStatus;

interface BetSpringDataRepository extends JpaRepository<BetJpaEntity, UUID> {

	Optional<BetJpaEntity> findByIdempotencyKey(String idempotencyKey);

	@Modifying
	@Query("UPDATE BetJpaEntity b SET b.status = :to WHERE b.id = :id AND b.status = :from")
	int transitionStatus(@Param("id") UUID id, @Param("from") BetStatus from, @Param("to") BetStatus to);

	// Filtros recebidos como um unico objeto (BetFilter, via SpEL #filter.campo()) para nao
	// estourar o limite de parametros do java:S107.
	// from/to comparados via COALESCE(coluna, campo), nao "(campo IS NULL OR coluna >= campo)":
	// Postgres nao consegue inferir o tipo de um parametro so usado num "IS NULL" isolado pra
	// coluna timestamp. Seguro so porque bet_date e NOT NULL - ver docs/CONVENTIONS.md.
	@Query("SELECT b FROM BetJpaEntity b WHERE "
			+ "(:#{#filter.bettingHouseId()} IS NULL OR b.bettingHouseId = :#{#filter.bettingHouseId()}) AND "
			+ "(:#{#filter.sportId()} IS NULL OR b.sportId = :#{#filter.sportId()}) AND "
			+ "(:#{#filter.leagueId()} IS NULL OR b.leagueId = :#{#filter.leagueId()}) AND "
			+ "(:#{#filter.marketId()} IS NULL OR b.marketId = :#{#filter.marketId()}) AND "
			+ "(:#{#filter.tipsterId()} IS NULL OR b.tipsterId = :#{#filter.tipsterId()}) AND "
			+ "b.betDate >= COALESCE(:#{#filter.from()}, b.betDate) AND "
			+ "b.betDate <= COALESCE(:#{#filter.to()}, b.betDate) "
			+ "ORDER BY b.betDate DESC")
	Page<BetJpaEntity> findFiltered(@Param("filter") BetFilter filter, Pageable pageable);
}
