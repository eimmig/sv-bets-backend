package com.stakevault.betting.bets.adapter.in.web;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.stakevault.betting.bets.domain.model.BetType;

import jakarta.validation.constraints.NotNull;

public record CreateBetRequest(@NotNull UUID bettingHouseId, @NotNull UUID sportId, @NotNull UUID leagueId,
		@NotNull UUID marketId, UUID tipsterId, String ticketNumber, String team1, String team2, String description,
		BetType betType, String playType, @NotNull BigDecimal stake, @NotNull BigDecimal odd,
		@NotNull Instant betDate) {
}
