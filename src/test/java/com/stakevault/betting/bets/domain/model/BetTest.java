package com.stakevault.betting.bets.domain.model;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class BetTest {

	@Test
	void shouldRejectNonPositiveStake() {
		assertThatThrownBy(() -> new Bet(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
				UUID.randomUUID(), null, UUID.randomUUID(), null, null, null, null, null, null, BigDecimal.ZERO,
				BigDecimal.valueOf(1.5), BetStatus.PENDING, Instant.now(), null))
				.isInstanceOf(IllegalArgumentException.class);
	}
}
