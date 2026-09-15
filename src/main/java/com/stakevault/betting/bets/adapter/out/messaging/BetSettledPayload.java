package com.stakevault.betting.bets.adapter.out.messaging;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.stakevault.betting.bets.domain.model.Bet;
import com.stakevault.betting.bets.domain.model.BetDimensionNames;
import com.stakevault.betting.bets.domain.model.BetResult;
import com.stakevault.betting.bets.domain.model.BetStatus;

// Payload de BetSettled e diferente de BetCreated (docs/contracts/bet-settled.schema.json) - so
// campos dimensionais + profit/settledAt, sem ticketNumber/description/betType/playType. TEAM
// entra aqui como dimensao (id+name, nullable) igual tipsterId/tipsterName - nao existia campo
// antigo a preservar (team1/team2 nunca fizeram parte deste payload), so campos novos aditivos.
public record BetSettledPayload(UUID betId, UUID bettingHouseId, String bettingHouseName, UUID sportId,
		String sportName, UUID leagueId, String leagueName, UUID marketId, String marketName, UUID tipsterId,
		String tipsterName, UUID team1Id, String team1Name, UUID team2Id, String team2Name, BigDecimal stake,
		BigDecimal odd, BetStatus status, BigDecimal profit, Instant settledAt) {

	static BetSettledPayload from(Bet bet, BetResult result, BetDimensionNames dimensionNames) {
		return new BetSettledPayload(bet.id(), bet.bettingHouseId(), dimensionNames.bettingHouseName(),
				bet.sportId(), dimensionNames.sportName(), bet.leagueId(), dimensionNames.leagueName(),
				bet.marketId(), dimensionNames.marketName(), bet.tipsterId(), dimensionNames.tipsterName(),
				bet.team1Id(), dimensionNames.team1Name(), bet.team2Id(), dimensionNames.team2Name(), bet.stake(),
				bet.odd(), bet.status(), result.profit(), result.settledAt());
	}
}
