package com.stakevault.betting.bets.domain.port.out;

import com.stakevault.betting.bets.domain.model.Bet;

public interface BetEventPublisher {

	void publishCreated(Bet bet);
}
