package com.stakevault.betting.bets.domain.port.in;

import com.stakevault.betting.bets.domain.model.League;
import com.stakevault.betting.bets.domain.model.PagedResult;

public interface LeagueCatalogUseCase {

	League create(String name);

	PagedResult<League> list(int page, int size);
}
