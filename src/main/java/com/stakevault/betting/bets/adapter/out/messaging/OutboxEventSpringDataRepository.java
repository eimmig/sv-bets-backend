package com.stakevault.betting.bets.adapter.out.messaging;

import org.springframework.data.jpa.repository.JpaRepository;

interface OutboxEventSpringDataRepository extends JpaRepository<OutboxEventJpaEntity, Long> {
}
