package com.stakevault.betting.bets.domain.model;

public class InvalidStatusTransitionException extends LocalizedRuntimeException {

	public InvalidStatusTransitionException(BetStatus from, BetStatus to) {
		super("invalid bet status transition: " + from + " -> " + to, from, to);
	}

	@Override
	public String messageKey() {
		return "error.invalid-status-transition";
	}

	@Override
	public int httpStatusCode() {
		return 422;
	}
}
