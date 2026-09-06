package com.stakevault.betting.bets.domain.port.out;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.stakevault.betting.bets.domain.model.BetResult;

public interface BetResultRepository {

	BetResult save(BetResult betResult);

	Optional<BetResult> findByBetId(UUID betId);

	// One query for the whole page, not one per betting house.
	Map<UUID, BigDecimal> sumProfitByBettingHouseIds(Collection<UUID> bettingHouseIds);
}
