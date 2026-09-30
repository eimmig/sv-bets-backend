package com.stakevault.betting.bets.adapter.out.messaging;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.stakevault.betting.bets.config.TenantContextHolder;
import com.stakevault.betting.bets.domain.model.Bet;
import com.stakevault.betting.bets.domain.model.BetDimensionNames;
import com.stakevault.betting.bets.domain.model.BetResult;
import com.stakevault.betting.bets.domain.port.out.BetEventPublisher;

import tools.jackson.databind.ObjectMapper;

@Component
public class OutboxBetEventPublisher implements BetEventPublisher {

	private static final String ROUTING_KEY_BET_CREATED = "bet.created";
	private static final String ROUTING_KEY_BET_SETTLED = "bet.settled";

	private final OutboxEventSpringDataRepository repository;
	private final ObjectMapper objectMapper;

	public OutboxBetEventPublisher(OutboxEventSpringDataRepository repository, ObjectMapper objectMapper) {
		this.repository = repository;
		this.objectMapper = objectMapper;
	}

	@Override
	public void publishCreated(Bet bet, BetDimensionNames dimensionNames) {
		BetEventEnvelope<BetCreatedPayload> envelope = new BetEventEnvelope<>(UUID.randomUUID(), "BetCreated", 1,
				Instant.now(), TenantContextHolder.current().slug(), bet.createdByUserId(),
				BetCreatedPayload.from(bet, dimensionNames));
		store(ROUTING_KEY_BET_CREATED, envelope, bet.id());
	}

	@Override
	public void publishSettled(Bet bet, BetResult result, BetDimensionNames dimensionNames) {
		BetEventEnvelope<BetSettledPayload> envelope = new BetEventEnvelope<>(UUID.randomUUID(), "BetSettled", 1,
				Instant.now(), TenantContextHolder.current().slug(), result.settledByUserId(),
				BetSettledPayload.from(bet, result, dimensionNames));
		store(ROUTING_KEY_BET_SETTLED, envelope, bet.id());
	}

	private void store(String routingKey, BetEventEnvelope<?> envelope, UUID betId) {
		String payload = objectMapper.writeValueAsString(envelope);
		repository.save(new OutboxEventJpaEntity(routingKey, payload, envelope.tenantId(), betId, envelope.occurredAt()));
	}
}
