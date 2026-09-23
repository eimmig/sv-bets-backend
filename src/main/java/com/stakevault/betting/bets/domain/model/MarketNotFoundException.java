package com.stakevault.betting.bets.domain.model;

import java.util.UUID;

public class MarketNotFoundException extends LocalizedRuntimeException {

	public MarketNotFoundException(UUID marketId) {
		super("market not found: " + marketId, marketId);
	}

	@Override
	public String messageKey() {
		return "error.market-not-found";
	}

	@Override
	public int httpStatusCode() {
		return 404;
	}
}
