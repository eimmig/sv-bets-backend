package com.stakevault.betting.bets.adapter.out.messaging;

import java.time.Instant;
import java.util.UUID;

// Envelope comum a todo evento publicado por este servico (docs/API-CONTRACTS.md) - correlationId
// fica de fora ate existir X-Correlation-Id de verdade (api-gateway, epic-008).
public record BetEventEnvelope<T>(UUID eventId, String eventType, int schemaVersion, Instant occurredAt,
		String tenantId, UUID userId, T payload) {
}
