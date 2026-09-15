package com.stakevault.betting.bets.domain.model;

import java.util.UUID;

public class TeamNotFoundException extends RuntimeException implements LocalizedDomainException {

	private final UUID teamId;

	public TeamNotFoundException(UUID teamId) {
		super("team not found: " + teamId);
		this.teamId = teamId;
	}

	@Override
	public String messageKey() {
		return "error.team-not-found";
	}

	@Override
	public int httpStatusCode() {
		return 404;
	}

	@Override
	public Object[] messageArgs() {
		return new Object[] { teamId };
	}
}
