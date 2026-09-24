package com.stakevault.betting.bets.adapter.out.messaging;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.stakevault.betting.bets.domain.model.Bet;
import com.stakevault.betting.bets.domain.model.BetDimensionNames;
import com.stakevault.betting.bets.domain.model.BetStatus;
import com.stakevault.betting.bets.domain.model.BetType;

public record BetCreatedPayload(UUID betId, UUID bettingHouseId, String bettingHouseName, UUID sportId,
		String sportName, UUID leagueId, String leagueName, UUID marketId, String marketName, UUID tipsterId,
		String tipsterName, String ticketNumber, UUID team1Id, String team1, UUID team2Id, String team2,
		String description, BetType betType, String playType, BigDecimal stake, BigDecimal odd, BetStatus status,
		Instant betDate) {

	static BetCreatedPayload from(Bet bet, BetDimensionNames dimensionNames) {
		return new BetCreatedPayload(bet.id(), bet.bettingHouseId(), dimensionNames.bettingHouseName(),
				bet.sportId(), dimensionNames.sportName(), bet.leagueId(), dimensionNames.leagueName(),
				bet.marketId(), dimensionNames.marketName(), bet.tipsterId(), dimensionNames.tipsterName(),
				bet.ticketNumber(), bet.team1Id(), dimensionNames.team1Name(), bet.team2Id(),
				dimensionNames.team2Name(), bet.description(), bet.betType(), bet.playType(), bet.stake(), bet.odd(),
				BetStatus.PENDING, bet.betDate());
	}
}
