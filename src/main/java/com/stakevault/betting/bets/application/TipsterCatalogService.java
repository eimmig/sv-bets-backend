package com.stakevault.betting.bets.application;

import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.stakevault.betting.bets.domain.model.CatalogAlreadyRegisteredException;
import com.stakevault.betting.bets.domain.model.PagedResult;
import com.stakevault.betting.bets.domain.model.Tipster;
import com.stakevault.betting.bets.domain.port.in.TipsterCatalogUseCase;
import com.stakevault.betting.bets.domain.port.out.TipsterRepository;

@Service
public class TipsterCatalogService implements TipsterCatalogUseCase {

	private final TipsterRepository tipsterRepository;

	public TipsterCatalogService(TipsterRepository tipsterRepository) {
		this.tipsterRepository = tipsterRepository;
	}

	@Override
	public Tipster create(String name) {
		if (tipsterRepository.existsByName(name)) {
			throw new CatalogAlreadyRegisteredException("tipster", name);
		}
		try {
			return tipsterRepository.save(new Tipster(UUID.randomUUID(), name));
		} catch (DataIntegrityViolationException raceLostToConcurrentInsert) {
			throw new CatalogAlreadyRegisteredException("tipster", name);
		}
	}

	@Override
	public PagedResult<Tipster> list(int page, int size) {
		return tipsterRepository.findAll(page, size);
	}
}
