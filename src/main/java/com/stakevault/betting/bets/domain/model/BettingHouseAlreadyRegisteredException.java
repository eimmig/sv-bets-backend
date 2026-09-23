package com.stakevault.betting.bets.domain.model;

public class BettingHouseAlreadyRegisteredException extends LocalizedRuntimeException {

	public BettingHouseAlreadyRegisteredException(String name) {
		super("betting house already registered: " + name, name == null ? "" : name);
	}

	@Override
	public String messageKey() {
		return "error.betting-house-already-registered";
	}

	@Override
	public int httpStatusCode() {
		return 409;
	}
}
