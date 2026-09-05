package com.stakevault.betting.bets.domain.port.out;

import com.stakevault.betting.bets.domain.model.League;
import com.stakevault.betting.bets.domain.model.PagedResult;

public interface LeagueRepository {

	League save(League league);

	boolean existsByName(String name);

	PagedResult<League> findAll(int page, int size);
}
