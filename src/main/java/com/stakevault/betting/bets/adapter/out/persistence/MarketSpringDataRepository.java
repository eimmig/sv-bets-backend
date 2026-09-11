package com.stakevault.betting.bets.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface MarketSpringDataRepository extends JpaRepository<MarketJpaEntity, UUID> {

	boolean existsByName(String name);
}
