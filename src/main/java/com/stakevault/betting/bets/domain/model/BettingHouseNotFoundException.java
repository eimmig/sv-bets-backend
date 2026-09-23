package com.stakevault.betting.bets.domain.model;

import java.util.UUID;

public class BettingHouseNotFoundException extends LocalizedRuntimeException {

	public BettingHouseNotFoundException(UUID bettingHouseId) {
		super("betting house not found: " + bettingHouseId, bettingHouseId);
	}

	@Override
	public String messageKey() {
		return "error.betting-house-not-found";
	}

	@Override
	public int httpStatusCode() {
		return 404;
	}
}
