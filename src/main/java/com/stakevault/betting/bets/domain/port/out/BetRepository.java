package com.stakevault.betting.bets.domain.port.out;

import java.util.Optional;
import java.util.UUID;

import com.stakevault.betting.bets.domain.model.Bet;
import com.stakevault.betting.bets.domain.model.BetFilter;
import com.stakevault.betting.bets.domain.model.BetStatus;
import com.stakevault.betting.bets.domain.model.PagedResult;

public interface BetRepository {

	Bet save(Bet bet);

	Optional<Bet> findById(UUID id);

	Optional<Bet> findByIdempotencyKey(String idempotencyKey);

	// Atomic conditional transition (UPDATE ... WHERE status = :from) - returns false without
	// writing anything if the current status no longer matches "from" (lost a concurrent race).
	boolean transitionStatus(UUID id, BetStatus from, BetStatus to);

	PagedResult<Bet> findFiltered(BetFilter filter, int page, int size);
}
