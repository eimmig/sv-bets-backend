package com.stakevault.betting.bets.domain.model;

import java.util.UUID;

public class TipsterNotFoundException extends RuntimeException implements LocalizedDomainException {

	private final UUID tipsterId;

	public TipsterNotFoundException(UUID tipsterId) {
		super("tipster not found: " + tipsterId);
		this.tipsterId = tipsterId;
	}

	@Override
	public String messageKey() {
		return "error.tipster-not-found";
	}

	@Override
	public int httpStatusCode() {
		return 404;
	}

	@Override
	public Object[] messageArgs() {
		return new Object[] { tipsterId };
	}
}
