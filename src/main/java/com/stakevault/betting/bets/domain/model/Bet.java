package com.stakevault.betting.bets.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Bet(UUID id, UUID bettingHouseId, UUID sportId, UUID leagueId, UUID marketId, UUID tipsterId,
		UUID createdByUserId, String ticketNumber, String team1, String team2, String description, String betType,
		String playType, BigDecimal stake, BigDecimal odd, BetStatus status, Instant betDate, String idempotencyKey) {

	public Bet {
		if (id == null || bettingHouseId == null || sportId == null || leagueId == null || marketId == null
				|| createdByUserId == null || stake == null || stake.signum() <= 0 || odd == null
				|| odd.compareTo(BigDecimal.ONE) <= 0 || status == null || betDate == null) {
			throw new IllegalArgumentException("dados de bet invalidos");
		}
	}
}
