package com.stakevault.betting.bets.domain.port.out;

import com.stakevault.betting.bets.domain.model.Bet;
import com.stakevault.betting.bets.domain.model.BetResult;

public interface BetEventPublisher {

	void publishCreated(Bet bet);

	void publishSettled(Bet bet, BetResult result);
}
