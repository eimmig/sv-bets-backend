package com.stakevault.betting.bets.domain.port.in;

import com.stakevault.betting.bets.domain.model.Bet;

// created=false means an Idempotency-Key replay returned the bet that already existed, not a new one.
public record BetCreationResult(Bet bet, boolean created) {
}
