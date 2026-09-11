package com.stakevault.betting.bets.domain.model;

public class InvalidStatusTransitionException extends RuntimeException implements LocalizedDomainException {

	private final BetStatus from;
	private final BetStatus to;

	public InvalidStatusTransitionException(BetStatus from, BetStatus to) {
		super("invalid bet status transition: " + from + " -> " + to);
		this.from = from;
		this.to = to;
	}

	@Override
	public String messageKey() {
		return "error.invalid-status-transition";
	}

	@Override
	public int httpStatusCode() {
		return 422;
	}

	@Override
	public Object[] messageArgs() {
		return new Object[] { from, to };
	}
}
