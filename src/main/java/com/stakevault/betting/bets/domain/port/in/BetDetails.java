package com.stakevault.betting.bets.domain.port.in;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.stakevault.betting.bets.domain.model.BetType;

public record BetDetails(UUID bettingHouseId, UUID sportId, UUID leagueId, UUID marketId, UUID tipsterId,
		String ticketNumber, UUID team1Id, UUID team2Id, String description, BetType betType, String playType,
		BigDecimal stake, BigDecimal odd, Instant betDate) {

	public static BetDetails of(BetFields fields) {
		return new BetDetails(fields.bettingHouseId(), fields.sportId(), fields.leagueId(), fields.marketId(),
				fields.tipsterId(), fields.ticketNumber(), fields.team1Id(), fields.team2Id(), fields.description(),
				fields.betType(), fields.playType(), fields.stake(), fields.odd(), fields.betDate());
	}
}
