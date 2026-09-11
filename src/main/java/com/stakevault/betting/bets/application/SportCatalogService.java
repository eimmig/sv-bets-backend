package com.stakevault.betting.bets.application;

import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.stakevault.betting.bets.domain.model.CatalogAlreadyRegisteredException;
import com.stakevault.betting.bets.domain.model.PagedResult;
import com.stakevault.betting.bets.domain.model.Sport;
import com.stakevault.betting.bets.domain.port.in.SportCatalogUseCase;
import com.stakevault.betting.bets.domain.port.out.SportRepository;

@Service
public class SportCatalogService implements SportCatalogUseCase {

	private final SportRepository sportRepository;

	public SportCatalogService(SportRepository sportRepository) {
		this.sportRepository = sportRepository;
	}

	@Override
	public Sport create(String name) {
		if (sportRepository.existsByName(name)) {
			throw new CatalogAlreadyRegisteredException("sport", name);
		}
		try {
			return sportRepository.save(new Sport(UUID.randomUUID(), name));
		} catch (DataIntegrityViolationException _) {
			throw new CatalogAlreadyRegisteredException("sport", name);
		}
	}

	@Override
	public PagedResult<Sport> list(int page, int size) {
		return sportRepository.findAll(page, size);
	}
}
