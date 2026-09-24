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

	boolean transitionStatus(UUID id, BetStatus from, BetStatus to);

	int updateFields(Bet updated, BetStatus expectedStatus);

	PagedResult<Bet> findFiltered(BetFilter filter, int page, int size);
}
