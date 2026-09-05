package com.stakevault.betting.bets.domain.model;

public class MissingCallerContextException extends RuntimeException implements LocalizedDomainException {

	public MissingCallerContextException() {
		super("missing or invalid X-User-Id");
	}

	@Override
	public String messageKey() {
		return "error.missing-caller-context";
	}

	@Override
	public int httpStatusCode() {
		return 401;
	}
}
