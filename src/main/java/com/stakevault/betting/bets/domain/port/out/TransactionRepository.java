package com.stakevault.betting.bets.domain.port.out;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import com.stakevault.betting.bets.domain.model.PagedResult;
import com.stakevault.betting.bets.domain.model.Transaction;

public interface TransactionRepository {

	Transaction save(Transaction transaction);

	PagedResult<Transaction> findFiltered(UUID bettingHouseId, Instant from, Instant to, int page, int size);

	Map<UUID, BigDecimal> sumNetAmountByBettingHouseIds(Collection<UUID> bettingHouseIds);

	BigDecimal sumNetAmountUpTo(Instant at);
}
