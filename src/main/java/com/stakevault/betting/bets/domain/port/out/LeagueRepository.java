package com.stakevault.betting.bets.domain.port.out;

import java.util.UUID;

import com.stakevault.betting.bets.domain.model.League;
import com.stakevault.betting.bets.domain.model.PagedResult;

public interface LeagueRepository {

	League save(League league);

	boolean existsByName(String name);

	boolean existsById(UUID id);

	PagedResult<League> findAll(int page, int size);
}
