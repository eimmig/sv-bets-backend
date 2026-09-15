package com.stakevault.betting.bets.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface TeamSpringDataRepository extends JpaRepository<TeamJpaEntity, UUID> {

	boolean existsByNameAndSportId(String name, UUID sportId);
}
