package com.stakevault.betting.bets.domain.port.in;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.stakevault.betting.bets.domain.model.BetType;

public interface BetFields {
	UUID bettingHouseId();

	UUID sportId();

	UUID leagueId();

	UUID marketId();

	UUID tipsterId();

	String ticketNumber();

	UUID team1Id();

	UUID team2Id();

	String description();

	BetType betType();

	String playType();

	BigDecimal stake();

	BigDecimal odd();

	Instant betDate();
}
