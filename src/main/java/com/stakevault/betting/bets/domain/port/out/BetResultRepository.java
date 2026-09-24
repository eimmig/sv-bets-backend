package com.stakevault.betting.bets.domain.port.out;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.stakevault.betting.bets.domain.model.BetResult;

public interface BetResultRepository {

	BetResult save(BetResult betResult);

	void updateProfit(UUID betId, BigDecimal profit);

	Optional<BetResult> findByBetId(UUID betId);

	Map<UUID, BigDecimal> sumProfitByBettingHouseIds(Collection<UUID> bettingHouseIds);

	BigDecimal sumProfitUpTo(Instant at);
}
