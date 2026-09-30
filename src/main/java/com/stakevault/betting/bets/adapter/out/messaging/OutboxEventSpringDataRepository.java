package com.stakevault.betting.bets.adapter.out.messaging;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface OutboxEventSpringDataRepository extends JpaRepository<OutboxEventJpaEntity, Long> {

	@Query(value = "SELECT * FROM public.outbox_event ORDER BY id LIMIT :limit FOR UPDATE SKIP LOCKED",
			nativeQuery = true)
	List<OutboxEventJpaEntity> lockNextBatch(@Param("limit") int limit);
}
