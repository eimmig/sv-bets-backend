package com.stakevault.betting.bets.domain.model;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class BetTest {

	@Test
	void shouldRejectNonPositiveStake() {
		UUID id = UUID.randomUUID();
		UUID bettingHouseId = UUID.randomUUID();
		UUID sportId = UUID.randomUUID();
		UUID leagueId = UUID.randomUUID();
		UUID marketId = UUID.randomUUID();
		UUID createdByUserId = UUID.randomUUID();
		BigDecimal nonPositiveStake = BigDecimal.ZERO;
		BigDecimal validOdd = BigDecimal.valueOf(1.5);
		Instant betDate = Instant.now();

		assertThatThrownBy(() -> new Bet(id, bettingHouseId, sportId, leagueId, marketId, null, createdByUserId, null,
				null, null, null, null, null, nonPositiveStake, validOdd, BetStatus.PENDING, betDate, null))
				.isInstanceOf(IllegalArgumentException.class);
	}
}
