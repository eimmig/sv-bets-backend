package com.stakevault.betting.bets.domain.port.out;

import com.stakevault.betting.bets.domain.model.Bet;
import com.stakevault.betting.bets.domain.model.BetDimensionNames;
import com.stakevault.betting.bets.domain.model.BetResult;

public interface BetEventPublisher {

	void publishCreated(Bet bet, BetDimensionNames dimensionNames);

	void publishSettled(Bet bet, BetResult result, BetDimensionNames dimensionNames);
}
