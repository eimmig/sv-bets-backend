package com.stakevault.betting.bets.domain.port.in;

import java.math.BigDecimal;
import java.util.UUID;

import com.stakevault.betting.bets.domain.model.PagedResult;
import com.stakevault.betting.bets.domain.model.Transaction;
import com.stakevault.betting.bets.domain.model.TransactionType;

public interface TransactionUseCase {

	Transaction create(UUID bettingHouseId, TransactionType type, BigDecimal amount);

	PagedResult<Transaction> list(UUID bettingHouseId, int page, int size);
}
