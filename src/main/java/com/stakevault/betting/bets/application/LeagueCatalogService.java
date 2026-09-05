package com.stakevault.betting.bets.application;

import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.stakevault.betting.bets.domain.model.CatalogAlreadyRegisteredException;
import com.stakevault.betting.bets.domain.model.League;
import com.stakevault.betting.bets.domain.model.PagedResult;
import com.stakevault.betting.bets.domain.port.in.LeagueCatalogUseCase;
import com.stakevault.betting.bets.domain.port.out.LeagueRepository;

@Service
public class LeagueCatalogService implements LeagueCatalogUseCase {

	private final LeagueRepository leagueRepository;

	public LeagueCatalogService(LeagueRepository leagueRepository) {
		this.leagueRepository = leagueRepository;
	}

	@Override
	public League create(String name) {
		if (leagueRepository.existsByName(name)) {
			throw new CatalogAlreadyRegisteredException("league", name);
		}
		try {
			return leagueRepository.save(new League(UUID.randomUUID(), name));
		} catch (DataIntegrityViolationException _) {
			throw new CatalogAlreadyRegisteredException("league", name);
		}
	}

	@Override
	public PagedResult<League> list(int page, int size) {
		return leagueRepository.findAll(page, size);
	}
}
