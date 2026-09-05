package com.stakevault.betting.bets.domain.model;

import java.math.BigDecimal;

public class InvalidStakeException extends RuntimeException implements LocalizedDomainException {

	private final BigDecimal stake;

	public InvalidStakeException(BigDecimal stake) {
		super("invalid stake: " + stake);
		this.stake = stake;
	}

	@Override
	public String messageKey() {
		return "error.invalid-stake";
	}

	@Override
	public int httpStatusCode() {
		return 422;
	}

	@Override
	public Object[] messageArgs() {
		return new Object[] { stake };
	}
}
