package com.stakevault.betting.bets.domain.model;

import java.util.UUID;

public class LeagueNotFoundException extends RuntimeException implements LocalizedDomainException {

	private final UUID leagueId;

	public LeagueNotFoundException(UUID leagueId) {
		super("league not found: " + leagueId);
		this.leagueId = leagueId;
	}

	@Override
	public String messageKey() {
		return "error.league-not-found";
	}

	@Override
	public int httpStatusCode() {
		return 404;
	}

	@Override
	public Object[] messageArgs() {
		return new Object[] { leagueId };
	}
}
