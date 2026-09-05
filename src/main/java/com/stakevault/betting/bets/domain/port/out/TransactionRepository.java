package com.stakevault.betting.bets.domain.port.out;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import com.stakevault.betting.bets.domain.model.PagedResult;
import com.stakevault.betting.bets.domain.model.Transaction;

public interface TransactionRepository {

	Transaction save(Transaction transaction);

	PagedResult<Transaction> findAll(int page, int size);

	PagedResult<Transaction> findByBettingHouseId(UUID bettingHouseId, int page, int size);

	// One query for the whole page, not one per betting house.
	Map<UUID, BigDecimal> sumNetAmountByBettingHouseIds(Collection<UUID> bettingHouseIds);
}
