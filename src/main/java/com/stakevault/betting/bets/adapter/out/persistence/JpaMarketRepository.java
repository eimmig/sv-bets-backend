package com.stakevault.betting.bets.adapter.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.stakevault.betting.bets.domain.model.Market;
import com.stakevault.betting.bets.domain.model.PagedResult;
import com.stakevault.betting.bets.domain.port.out.MarketRepository;

@Repository
public class JpaMarketRepository implements MarketRepository {

	private final MarketSpringDataRepository jpaRepository;

	public JpaMarketRepository(MarketSpringDataRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public Market save(Market market) {
		MarketJpaEntity saved = jpaRepository.save(new MarketJpaEntity(market.id(), market.name()));
		return toDomain(saved);
	}

	@Override
	public boolean existsByName(String name) {
		return jpaRepository.existsByName(name);
	}

	@Override
	public PagedResult<Market> findAll(int page, int size) {
		Page<MarketJpaEntity> result = jpaRepository.findAll(PageRequest.of(page, size));
		return new PagedResult<>(result.getContent().stream().map(JpaMarketRepository::toDomain).toList(),
				result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
	}

	private static Market toDomain(MarketJpaEntity entity) {
		return new Market(entity.getId(), entity.getName());
	}
}
