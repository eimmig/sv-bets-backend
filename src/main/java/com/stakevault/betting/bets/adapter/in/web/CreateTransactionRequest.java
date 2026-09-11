package com.stakevault.betting.bets.adapter.in.web;

import java.math.BigDecimal;
import java.util.UUID;

import com.stakevault.betting.bets.domain.model.TransactionType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateTransactionRequest(@NotNull UUID bettingHouseId, @NotNull TransactionType type,
		@NotNull @Positive BigDecimal amount) {
}
