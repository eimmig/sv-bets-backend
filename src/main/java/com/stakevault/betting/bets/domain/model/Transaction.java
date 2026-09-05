package com.stakevault.betting.bets.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Transaction(UUID id, UUID bettingHouseId, TransactionType type, BigDecimal amount, Instant createdAt) {

	public Transaction {
		if (id == null || bettingHouseId == null || type == null || createdAt == null
				|| amount == null || amount.signum() <= 0) {
			throw new IllegalArgumentException("dados de transaction invalidos");
		}
	}
}
