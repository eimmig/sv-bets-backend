package com.stakevault.betting.bets.domain.model;

import java.util.UUID;

public class TipsterNotFoundException extends LocalizedRuntimeException {

	public TipsterNotFoundException(UUID tipsterId) {
		super("tipster not found: " + tipsterId, tipsterId);
	}

	@Override
	public String messageKey() {
		return "error.tipster-not-found";
	}

	@Override
	public int httpStatusCode() {
		return 404;
	}
}
