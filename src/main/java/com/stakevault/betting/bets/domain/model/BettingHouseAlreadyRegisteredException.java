package com.stakevault.betting.bets.domain.model;

public class BettingHouseAlreadyRegisteredException extends RuntimeException implements LocalizedDomainException {

	private final String name;

	public BettingHouseAlreadyRegisteredException(String name) {
		super("betting house already registered: " + name);
		this.name = name;
	}

	@Override
	public String messageKey() {
		return "error.betting-house-already-registered";
	}

	@Override
	public int httpStatusCode() {
		return 409;
	}

	@Override
	public Object[] messageArgs() {
		return new Object[] { name == null ? "" : name };
	}
}
