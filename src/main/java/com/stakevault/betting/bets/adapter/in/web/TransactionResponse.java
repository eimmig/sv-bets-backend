package com.stakevault.betting.bets.adapter.in.web;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.stakevault.betting.bets.domain.model.Transaction;
import com.stakevault.betting.bets.domain.model.TransactionType;

public record TransactionResponse(UUID id, UUID bettingHouseId, TransactionType type, BigDecimal amount,
		Instant createdAt) {

	static TransactionResponse from(Transaction transaction) {
		return new TransactionResponse(transaction.id(), transaction.bettingHouseId(), transaction.type(),
				transaction.amount(), transaction.createdAt());
	}
}
