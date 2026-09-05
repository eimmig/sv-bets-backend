package com.stakevault.betting.bets.application;

import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.stakevault.betting.bets.domain.model.CatalogAlreadyRegisteredException;
import com.stakevault.betting.bets.domain.model.Market;
import com.stakevault.betting.bets.domain.model.PagedResult;
import com.stakevault.betting.bets.domain.port.in.MarketCatalogUseCase;
import com.stakevault.betting.bets.domain.port.out.MarketRepository;

@Service
public class MarketCatalogService implements MarketCatalogUseCase {

	private final MarketRepository marketRepository;

	public MarketCatalogService(MarketRepository marketRepository) {
		this.marketRepository = marketRepository;
	}

	@Override
	public Market create(String name) {
		if (marketRepository.existsByName(name)) {
			throw new CatalogAlreadyRegisteredException("market", name);
		}
		try {
			return marketRepository.save(new Market(UUID.randomUUID(), name));
		} catch (DataIntegrityViolationException _) {
			throw new CatalogAlreadyRegisteredException("market", name);
		}
	}

	@Override
	public PagedResult<Market> list(int page, int size) {
		return marketRepository.findAll(page, size);
	}
}
