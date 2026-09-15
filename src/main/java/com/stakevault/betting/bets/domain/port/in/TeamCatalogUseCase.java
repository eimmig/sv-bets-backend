package com.stakevault.betting.bets.domain.port.in;

import java.util.UUID;

import com.stakevault.betting.bets.domain.model.PagedResult;
import com.stakevault.betting.bets.domain.model.Team;

public interface TeamCatalogUseCase {

	Team create(String name, UUID sportId);

	PagedResult<Team> list(int page, int size);
}
