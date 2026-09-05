package com.stakevault.betting.bets.domain.port.in;

import com.stakevault.betting.bets.domain.model.Market;
import com.stakevault.betting.bets.domain.model.PagedResult;

public interface MarketCatalogUseCase {

	Market create(String name);

	PagedResult<Market> list(int page, int size);
}
