package com.stakevault.betting.bets.domain.port.out;

import java.util.Optional;
import java.util.UUID;

import com.stakevault.betting.bets.domain.model.BetResult;

public interface BetResultRepository {

	BetResult save(BetResult betResult);

	Optional<BetResult> findByBetId(UUID betId);
}
