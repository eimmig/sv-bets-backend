package com.stakevault.betting.bets.domain.model;

import java.util.UUID;

public class SportNotFoundException extends LocalizedRuntimeException {

	public SportNotFoundException(UUID sportId) {
		super("sport not found: " + sportId, sportId);
	}

	@Override
	public String messageKey() {
		return "error.sport-not-found";
	}

	@Override
	public int httpStatusCode() {
		return 404;
	}
}
