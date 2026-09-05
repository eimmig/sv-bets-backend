package com.stakevault.betting.bets.domain.port.out;

import com.stakevault.betting.bets.domain.model.PagedResult;
import com.stakevault.betting.bets.domain.model.Sport;

public interface SportRepository {

	Sport save(Sport sport);

	boolean existsByName(String name);

	PagedResult<Sport> findAll(int page, int size);
}
