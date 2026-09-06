package com.stakevault.betting.bets.adapter.out.messaging;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.stakevault.betting.bets.domain.model.Bet;
import com.stakevault.betting.bets.domain.model.BetResult;
import com.stakevault.betting.bets.domain.model.BetStatus;

// Payload de BetSettled e diferente de BetCreated (docs/contracts/bet-settled.schema.json) - so
// campos dimensionais + profit/settledAt, sem ticketNumber/team1/team2/description/betType/playType.
public record BetSettledPayload(UUID betId, UUID bettingHouseId, UUID sportId, UUID leagueId, UUID marketId,
		UUID tipsterId, BigDecimal stake, BigDecimal odd, BetStatus status, BigDecimal profit, Instant settledAt) {

	static BetSettledPayload from(Bet bet, BetResult result) {
		return new BetSettledPayload(bet.id(), bet.bettingHouseId(), bet.sportId(), bet.leagueId(), bet.marketId(),
				bet.tipsterId(), bet.stake(), bet.odd(), bet.status(), result.profit(), result.settledAt());
	}
}
