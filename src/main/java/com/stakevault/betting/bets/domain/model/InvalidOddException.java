package com.stakevault.betting.bets.domain.model;

import java.math.BigDecimal;

public class InvalidOddException extends LocalizedRuntimeException {

	public InvalidOddException(BigDecimal odd) {
		super("invalid odd: " + odd, odd);
	}

	@Override
	public String messageKey() {
		return "error.invalid-odd";
	}

	@Override
	public int httpStatusCode() {
		return 422;
	}
}
