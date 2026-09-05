package com.stakevault.betting.bets.domain.port.in;

import com.stakevault.betting.bets.domain.model.PagedResult;
import com.stakevault.betting.bets.domain.model.Sport;

public interface SportCatalogUseCase {

	Sport create(String name);

	PagedResult<Sport> list(int page, int size);
}
