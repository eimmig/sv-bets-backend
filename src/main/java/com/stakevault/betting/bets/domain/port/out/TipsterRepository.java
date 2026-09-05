package com.stakevault.betting.bets.domain.port.out;

import com.stakevault.betting.bets.domain.model.PagedResult;
import com.stakevault.betting.bets.domain.model.Tipster;

public interface TipsterRepository {

	Tipster save(Tipster tipster);

	boolean existsByName(String name);

	PagedResult<Tipster> findAll(int page, int size);
}
