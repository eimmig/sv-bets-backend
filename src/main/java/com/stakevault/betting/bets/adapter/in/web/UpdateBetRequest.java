package com.stakevault.betting.bets.adapter.in.web;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.stakevault.betting.bets.domain.model.BetStatus;
import com.stakevault.betting.bets.domain.model.BetType;

import jakarta.validation.constraints.NotNull;

public record UpdateBetRequest(@NotNull UUID bettingHouseId, @NotNull UUID sportId, @NotNull UUID leagueId,
		@NotNull UUID marketId, UUID tipsterId, String ticketNumber, UUID team1Id, UUID team2Id, String description,
		BetType betType, String playType, @NotNull BigDecimal stake, @NotNull BigDecimal odd,
		@NotNull Instant betDate, @NotNull BetStatus status) {
}
