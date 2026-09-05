package com.stakevault.betting.bets.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface BetResultSpringDataRepository extends JpaRepository<BetResultJpaEntity, UUID> {

	Optional<BetResultJpaEntity> findByBetId(UUID betId);
}
