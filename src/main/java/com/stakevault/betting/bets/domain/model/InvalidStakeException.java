package com.stakevault.betting.bets.domain.model;

import java.math.BigDecimal;

public class InvalidStakeException extends LocalizedRuntimeException {

	public InvalidStakeException(BigDecimal stake) {
		super("invalid stake: " + stake, stake);
	}

	@Override
	public String messageKey() {
		return "error.invalid-stake";
	}

	@Override
	public int httpStatusCode() {
		return 422;
	}
}
