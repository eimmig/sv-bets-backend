package com.stakevault.betting.bets.domain.port.in;

import com.stakevault.betting.bets.domain.model.PagedResult;
import com.stakevault.betting.bets.domain.model.Tipster;

public interface TipsterCatalogUseCase {

	Tipster create(String name);

	PagedResult<Tipster> list(int page, int size);
}
