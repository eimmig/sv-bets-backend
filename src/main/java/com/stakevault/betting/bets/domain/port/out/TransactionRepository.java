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

	// One query for the whole page, not one per betting house.
	Map<UUID, BigDecimal> sumNetAmountByBettingHouseIds(Collection<UUID> bettingHouseIds);

	// Todas as casas do tenant, nao agrupado - "at" e o limite superior exclusivo ja resolvido
	// (fim do dia civil brasileiro convertido pra Instant/UTC). Usado por feat-014.3.
	BigDecimal sumNetAmountUpTo(Instant at);
}
