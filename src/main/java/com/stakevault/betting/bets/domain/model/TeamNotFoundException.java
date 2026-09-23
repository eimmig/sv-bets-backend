package com.stakevault.betting.bets.domain.model;

import java.util.UUID;

public class TeamNotFoundException extends LocalizedRuntimeException {

	public TeamNotFoundException(UUID teamId) {
		super("team not found: " + teamId, teamId);
	}

	@Override
	public String messageKey() {
		return "error.team-not-found";
	}

	@Override
	public int httpStatusCode() {
		return 404;
	}
}
