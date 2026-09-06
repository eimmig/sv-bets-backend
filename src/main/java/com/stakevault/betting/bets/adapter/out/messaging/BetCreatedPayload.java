package com.stakevault.betting.bets.adapter.out.messaging;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.stakevault.betting.bets.domain.model.Bet;
import com.stakevault.betting.bets.domain.model.BetStatus;

public record BetCreatedPayload(UUID betId, UUID bettingHouseId, UUID sportId, UUID leagueId, UUID marketId,
		UUID tipsterId, String ticketNumber, String team1, String team2, String description, String betType,
		String playType, BigDecimal stake, BigDecimal odd, BetStatus status, Instant betDate) {

	static BetCreatedPayload from(Bet bet) {
		return new BetCreatedPayload(bet.id(), bet.bettingHouseId(), bet.sportId(), bet.leagueId(), bet.marketId(),
				bet.tipsterId(), bet.ticketNumber(), bet.team1(), bet.team2(), bet.description(), bet.betType(),
				bet.playType(), bet.stake(), bet.odd(), BetStatus.PENDING, bet.betDate());
	}
}
