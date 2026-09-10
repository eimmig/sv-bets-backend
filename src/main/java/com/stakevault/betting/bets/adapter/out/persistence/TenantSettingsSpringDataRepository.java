package com.stakevault.betting.bets.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface TenantSettingsSpringDataRepository extends JpaRepository<TenantSettingsJpaEntity, UUID> {
}
