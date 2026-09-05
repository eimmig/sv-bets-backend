package com.stakevault.betting.bets.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record BettingHouse(UUID id, String name, BigDecimal initialBalance, Instant createdAt) {

	public BettingHouse {
		if (id == null || name == null || name.isBlank() || initialBalance == null
				|| initialBalance.signum() < 0 || createdAt == null) {
			throw new IllegalArgumentException("dados de betting house invalidos");
		}
	}
}
