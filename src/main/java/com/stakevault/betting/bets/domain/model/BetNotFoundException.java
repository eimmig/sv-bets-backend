package com.stakevault.betting.bets.domain.model;

import java.util.UUID;

public class BetNotFoundException extends LocalizedRuntimeException {

	public BetNotFoundException(UUID betId) {
		super("bet not found: " + betId, betId);
	}

	@Override
	public String messageKey() {
		return "error.bet-not-found";
	}

	@Override
	public int httpStatusCode() {
		return 404;
	}
}
