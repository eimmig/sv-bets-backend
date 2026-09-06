package com.stakevault.betting.bets.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;

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
}
