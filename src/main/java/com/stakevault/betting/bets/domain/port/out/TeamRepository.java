package com.stakevault.betting.bets.domain.port.out;

import java.util.Optional;
import java.util.UUID;

import com.stakevault.betting.bets.domain.model.PagedResult;
import com.stakevault.betting.bets.domain.model.Team;

public interface TeamRepository {

	Team save(Team team);

	boolean existsByNameAndSportId(String name, UUID sportId);

	boolean existsById(UUID id);

	Optional<Team> findById(UUID id);

	PagedResult<Team> findAll(int page, int size);
}
