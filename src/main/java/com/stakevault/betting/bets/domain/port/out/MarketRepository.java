package com.stakevault.betting.bets.domain.port.out;

import com.stakevault.betting.bets.domain.model.Market;
import com.stakevault.betting.bets.domain.model.PagedResult;

public interface MarketRepository {

	Market save(Market market);

	boolean existsByName(String name);

	PagedResult<Market> findAll(int page, int size);
}
