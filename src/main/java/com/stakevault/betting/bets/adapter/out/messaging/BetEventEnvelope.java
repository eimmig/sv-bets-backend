package com.stakevault.betting.bets.adapter.out.messaging;

import java.time.Instant;
import java.util.UUID;

public record BetEventEnvelope<T>(UUID eventId, String eventType, int schemaVersion, Instant occurredAt,
		String tenantId, UUID userId, T payload) {
}
