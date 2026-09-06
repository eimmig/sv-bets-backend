package com.stakevault.betting.bets.domain.model;

import java.time.Instant;
import java.util.UUID;

// Todos os campos opcionais e combinaveis (RN08) - null significa "sem filtro naquele campo".
public record BetFilter(UUID bettingHouseId, UUID sportId, UUID leagueId, UUID marketId, UUID tipsterId, Instant from,
		Instant to) {
}
