package com.stakevault.betting.bets.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BankrollBalance(LocalDate at, BigDecimal balance) {

	public BankrollBalance {
		if (at == null || balance == null) {
			throw new IllegalArgumentException("dados de bankroll balance invalidos");
		}
	}
}
