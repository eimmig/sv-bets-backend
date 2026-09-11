package com.stakevault.betting.bets.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface SportSpringDataRepository extends JpaRepository<SportJpaEntity, UUID> {

	boolean existsByName(String name);
}
