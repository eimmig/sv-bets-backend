package com.stakevault.betting.bets.domain.port.in;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreateBetCommand(UUID callerId, UUID bettingHouseId, UUID sportId, UUID leagueId, UUID marketId,
		UUID tipsterId, String ticketNumber, String team1, String team2, String description, String betType,
		String playType, BigDecimal stake, BigDecimal odd, Instant betDate, String idempotencyKey) {
}
