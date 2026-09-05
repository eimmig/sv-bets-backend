package com.stakevault.betting.bets.domain.port.out;

import java.util.UUID;

import com.stakevault.betting.bets.domain.model.Market;
import com.stakevault.betting.bets.domain.model.PagedResult;

public interface MarketRepository {

	Market save(Market market);

	boolean existsByName(String name);

	boolean existsById(UUID id);

	PagedResult<Market> findAll(int page, int size);
}
