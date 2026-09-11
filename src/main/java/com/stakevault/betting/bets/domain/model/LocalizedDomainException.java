package com.stakevault.betting.bets.domain.model;

public interface LocalizedDomainException {

	String messageKey();

	int httpStatusCode();

	default Object[] messageArgs() {
		return new Object[0];
	}
}
