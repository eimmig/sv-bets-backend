package com.stakevault.betting.bets.application;

import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.stakevault.betting.bets.domain.model.CatalogAlreadyRegisteredException;
import com.stakevault.betting.bets.domain.model.PagedResult;
import com.stakevault.betting.bets.domain.model.SportNotFoundException;
import com.stakevault.betting.bets.domain.model.Team;
import com.stakevault.betting.bets.domain.port.in.TeamCatalogUseCase;
import com.stakevault.betting.bets.domain.port.out.SportRepository;
import com.stakevault.betting.bets.domain.port.out.TeamRepository;

@Service
public class TeamCatalogService implements TeamCatalogUseCase {

	private final TeamRepository teamRepository;
	private final SportRepository sportRepository;

	public TeamCatalogService(TeamRepository teamRepository, SportRepository sportRepository) {
		this.teamRepository = teamRepository;
		this.sportRepository = sportRepository;
	}

	@Override
	public Team create(String name, UUID sportId) {
		if (!sportRepository.existsById(sportId)) {
			throw new SportNotFoundException(sportId);
		}
		if (teamRepository.existsByNameAndSportId(name, sportId)) {
			throw new CatalogAlreadyRegisteredException("team", name);
		}
		try {
			return teamRepository.save(new Team(UUID.randomUUID(), name, sportId));
		} catch (DataIntegrityViolationException _) {
			throw new CatalogAlreadyRegisteredException("team", name);
		}
	}

	@Override
	public PagedResult<Team> list(int page, int size) {
		return teamRepository.findAll(page, size);
	}
}
