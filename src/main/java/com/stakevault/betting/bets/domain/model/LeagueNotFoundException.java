package com.stakevault.betting.bets.domain.model;

import java.util.UUID;

public class LeagueNotFoundException extends LocalizedRuntimeException {

	public LeagueNotFoundException(UUID leagueId) {
		super("league not found: " + leagueId, leagueId);
	}

	@Override
	public String messageKey() {
		return "error.league-not-found";
	}

	@Override
	public int httpStatusCode() {
		return 404;
	}
}
