package com.stakevault.betting.bets.domain.model;

import java.util.UUID;

public class BetConcurrentlyModifiedException extends LocalizedRuntimeException {

	public BetConcurrentlyModifiedException(UUID betId) {
		super("bet modified concurrently: " + betId, betId);
	}

	@Override
	public String messageKey() {
		return "error.bet-modified-concurrently";
	}

	@Override
	public int httpStatusCode() {
		return 409;
	}
}
