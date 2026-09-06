package com.stakevault.betting.bets.adapter.out.messaging;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.stakevault.betting.bets.domain.model.Bet;
import com.stakevault.betting.bets.domain.model.BetDimensionNames;
import com.stakevault.betting.bets.domain.model.BetStatus;

public record BetCreatedPayload(UUID betId, UUID bettingHouseId, String bettingHouseName, UUID sportId,
		String sportName, UUID leagueId, String leagueName, UUID marketId, String marketName, UUID tipsterId,
		String tipsterName, String ticketNumber, String team1, String team2, String description, String betType,
		String playType, BigDecimal stake, BigDecimal odd, BetStatus status, Instant betDate) {

	static BetCreatedPayload from(Bet bet, BetDimensionNames dimensionNames) {
		return new BetCreatedPayload(bet.id(), bet.bettingHouseId(), dimensionNames.bettingHouseName(),
				bet.sportId(), dimensionNames.sportName(), bet.leagueId(), dimensionNames.leagueName(),
				bet.marketId(), dimensionNames.marketName(), bet.tipsterId(), dimensionNames.tipsterName(),
				bet.ticketNumber(), bet.team1(), bet.team2(), bet.description(), bet.betType(), bet.playType(),
				bet.stake(), bet.odd(), BetStatus.PENDING, bet.betDate());
	}
}
