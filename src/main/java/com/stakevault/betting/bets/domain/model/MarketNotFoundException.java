package com.stakevault.betting.bets.domain.model;

import java.util.UUID;

public class MarketNotFoundException extends RuntimeException implements LocalizedDomainException {

	private final UUID marketId;

	public MarketNotFoundException(UUID marketId) {
		super("market not found: " + marketId);
		this.marketId = marketId;
	}

	@Override
	public String messageKey() {
		return "error.market-not-found";
	}

	@Override
	public int httpStatusCode() {
		return 404;
	}

	@Override
	public Object[] messageArgs() {
		return new Object[] { marketId };
	}
}
