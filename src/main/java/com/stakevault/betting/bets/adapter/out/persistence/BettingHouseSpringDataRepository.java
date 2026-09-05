package com.stakevault.betting.bets.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface BettingHouseSpringDataRepository extends JpaRepository<BettingHouseJpaEntity, UUID> {

	boolean existsByName(String name);
}
