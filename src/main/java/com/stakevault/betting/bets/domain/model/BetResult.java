package com.stakevault.betting.bets.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record BetResult(UUID id, UUID betId, UUID settledByUserId, BigDecimal profit, Instant settledAt) {

	public BetResult {
		if (id == null || betId == null || settledByUserId == null || profit == null || settledAt == null) {
			throw new IllegalArgumentException("dados de bet result invalidos");
		}
	}
}
