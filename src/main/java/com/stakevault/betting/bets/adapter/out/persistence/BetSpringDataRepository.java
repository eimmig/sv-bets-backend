package com.stakevault.betting.bets.adapter.out.persistence;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.stakevault.betting.bets.domain.model.BetStatus;

interface BetSpringDataRepository extends JpaRepository<BetJpaEntity, UUID> {

	Optional<BetJpaEntity> findByIdempotencyKey(String idempotencyKey);

	@Modifying
	@Query("UPDATE BetJpaEntity b SET b.status = :to WHERE b.id = :id AND b.status = :from")
	int transitionStatus(@Param("id") UUID id, @Param("from") BetStatus from, @Param("to") BetStatus to);

	@Query("SELECT b FROM BetJpaEntity b WHERE "
			+ "(:bettingHouseId IS NULL OR b.bettingHouseId = :bettingHouseId) AND "
			+ "(:sportId IS NULL OR b.sportId = :sportId) AND "
			+ "(:leagueId IS NULL OR b.leagueId = :leagueId) AND "
			+ "(:marketId IS NULL OR b.marketId = :marketId) AND "
			+ "(:tipsterId IS NULL OR b.tipsterId = :tipsterId) AND "
			+ "(:from IS NULL OR b.betDate >= :from) AND "
			+ "(:to IS NULL OR b.betDate <= :to) "
			+ "ORDER BY b.betDate DESC")
	Page<BetJpaEntity> findFiltered(@Param("bettingHouseId") UUID bettingHouseId, @Param("sportId") UUID sportId,
			@Param("leagueId") UUID leagueId, @Param("marketId") UUID marketId, @Param("tipsterId") UUID tipsterId,
			@Param("from") Instant from, @Param("to") Instant to, Pageable pageable);
}
