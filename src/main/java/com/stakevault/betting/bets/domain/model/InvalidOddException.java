package com.stakevault.betting.bets.domain.model;

import java.math.BigDecimal;

public class InvalidOddException extends RuntimeException implements LocalizedDomainException {

	private final BigDecimal odd;

	public InvalidOddException(BigDecimal odd) {
		super("invalid odd: " + odd);
		this.odd = odd;
	}

	@Override
	public String messageKey() {
		return "error.invalid-odd";
	}

	@Override
	public int httpStatusCode() {
		return 422;
	}

	@Override
	public Object[] messageArgs() {
		return new Object[] { odd };
	}
}
