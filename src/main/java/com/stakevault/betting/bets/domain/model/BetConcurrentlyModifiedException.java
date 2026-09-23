package com.stakevault.betting.bets.domain.model;

import java.util.UUID;

public class BetConcurrentlyModifiedException extends RuntimeException implements LocalizedDomainException {

	private final UUID betId;

	public BetConcurrentlyModifiedException(UUID betId) {
		super("bet modified concurrently: " + betId);
		this.betId = betId;
	}

	@Override
	public String messageKey() {
		return "error.bet-modified-concurrently";
	}

	@Override
	public int httpStatusCode() {
		return 409;
	}

	@Override
	public Object[] messageArgs() {
		return new Object[] { betId };
	}
}
