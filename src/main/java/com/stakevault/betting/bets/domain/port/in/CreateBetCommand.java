package com.stakevault.betting.bets.domain.port.in;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.stakevault.betting.bets.domain.model.BetType;

public record CreateBetCommand(UUID callerId, UUID bettingHouseId, UUID sportId, UUID leagueId, UUID marketId,
		UUID tipsterId, String ticketNumber, String team1, String team2, String description, BetType betType,
		String playType, BigDecimal stake, BigDecimal odd, Instant betDate, String idempotencyKey) {
}
