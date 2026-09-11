package com.stakevault.betting.bets.domain.model;

import java.util.UUID;

public class SportNotFoundException extends RuntimeException implements LocalizedDomainException {

	private final UUID sportId;

	public SportNotFoundException(UUID sportId) {
		super("sport not found: " + sportId);
		this.sportId = sportId;
	}

	@Override
	public String messageKey() {
		return "error.sport-not-found";
	}

	@Override
	public int httpStatusCode() {
		return 404;
	}

	@Override
	public Object[] messageArgs() {
		return new Object[] { sportId };
	}
}
