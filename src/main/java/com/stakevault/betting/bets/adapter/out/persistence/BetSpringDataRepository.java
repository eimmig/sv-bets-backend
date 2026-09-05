package com.stakevault.betting.bets.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface BetSpringDataRepository extends JpaRepository<BetJpaEntity, UUID> {

	Optional<BetJpaEntity> findByIdempotencyKey(String idempotencyKey);
}
