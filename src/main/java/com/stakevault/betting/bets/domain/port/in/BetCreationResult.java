package com.stakevault.betting.bets.domain.port.in;

import com.stakevault.betting.bets.domain.model.Bet;

public record BetCreationResult(Bet bet, boolean created) {
}
