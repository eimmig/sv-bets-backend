package com.stakevault.betting.bets.adapter.in.web;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.stakevault.betting.bets.domain.model.Bet;
import com.stakevault.betting.bets.domain.model.BetStatus;

public record BetResponse(UUID id, UUID bettingHouseId, UUID sportId, UUID leagueId, UUID marketId, UUID tipsterId,
		UUID createdByUserId, String ticketNumber, String team1, String team2, String description, String betType,
		String playType, BigDecimal stake, BigDecimal odd, BetStatus status, Instant betDate) {

	static BetResponse from(Bet bet) {
		return new BetResponse(bet.id(), bet.bettingHouseId(), bet.sportId(), bet.leagueId(), bet.marketId(),
				bet.tipsterId(), bet.createdByUserId(), bet.ticketNumber(), bet.team1(), bet.team2(),
				bet.description(), bet.betType(), bet.playType(), bet.stake(), bet.odd(), bet.status(), bet.betDate());
	}
}
