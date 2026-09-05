package com.stakevault.betting.bets.domain.model;

import java.util.UUID;

public class BetNotFoundException extends RuntimeException implements LocalizedDomainException {

	private final UUID betId;

	public BetNotFoundException(UUID betId) {
		super("bet not found: " + betId);
		this.betId = betId;
	}

	@Override
	public String messageKey() {
		return "error.bet-not-found";
	}

	@Override
	public int httpStatusCode() {
		return 404;
	}

	@Override
	public Object[] messageArgs() {
		return new Object[] { betId };
	}
}
