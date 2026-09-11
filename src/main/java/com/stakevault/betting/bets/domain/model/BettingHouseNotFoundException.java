package com.stakevault.betting.bets.domain.model;

import java.util.UUID;

public class BettingHouseNotFoundException extends RuntimeException implements LocalizedDomainException {

	private final UUID bettingHouseId;

	public BettingHouseNotFoundException(UUID bettingHouseId) {
		super("betting house not found: " + bettingHouseId);
		this.bettingHouseId = bettingHouseId;
	}

	@Override
	public String messageKey() {
		return "error.betting-house-not-found";
	}

	@Override
	public int httpStatusCode() {
		return 404;
	}

	@Override
	public Object[] messageArgs() {
		return new Object[] { bettingHouseId };
	}
}
