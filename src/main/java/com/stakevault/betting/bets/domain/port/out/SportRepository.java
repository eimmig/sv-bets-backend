package com.stakevault.betting.bets.domain.port.out;

import java.util.UUID;

import com.stakevault.betting.bets.domain.model.PagedResult;
import com.stakevault.betting.bets.domain.model.Sport;

public interface SportRepository {

	Sport save(Sport sport);

	boolean existsByName(String name);

	boolean existsById(UUID id);

	PagedResult<Sport> findAll(int page, int size);
}
