package com.stakevault.betting.bets.domain.model;

import java.time.Instant;
import java.util.UUID;

public record BetFilter(UUID bettingHouseId, UUID sportId, UUID leagueId, UUID marketId, UUID tipsterId, Instant from,
		Instant to) {
}
