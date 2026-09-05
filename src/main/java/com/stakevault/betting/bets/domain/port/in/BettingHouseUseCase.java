package com.stakevault.betting.bets.domain.port.in;

import java.math.BigDecimal;

import com.stakevault.betting.bets.domain.model.BettingHouseBalance;
import com.stakevault.betting.bets.domain.model.PagedResult;

public interface BettingHouseUseCase {

	BettingHouseBalance create(String name, BigDecimal initialBalance);

	PagedResult<BettingHouseBalance> list(int page, int size);
}
