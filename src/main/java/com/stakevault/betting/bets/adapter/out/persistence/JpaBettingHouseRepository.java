package com.stakevault.betting.bets.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.stakevault.betting.bets.domain.model.BettingHouse;
import com.stakevault.betting.bets.domain.model.PagedResult;
import com.stakevault.betting.bets.domain.port.out.BettingHouseRepository;

@Repository
public class JpaBettingHouseRepository implements BettingHouseRepository {

	private final BettingHouseSpringDataRepository jpaRepository;

	public JpaBettingHouseRepository(BettingHouseSpringDataRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public BettingHouse save(BettingHouse bettingHouse) {
		BettingHouseJpaEntity saved = jpaRepository.save(new BettingHouseJpaEntity(bettingHouse.id(),
				bettingHouse.name(), bettingHouse.initialBalance(), bettingHouse.createdAt()));
		return toDomain(saved);
	}

	@Override
	public boolean existsByName(String name) {
		return jpaRepository.existsByName(name);
	}

	@Override
	public boolean existsById(UUID id) {
		return jpaRepository.existsById(id);
	}

	@Override
	public PagedResult<BettingHouse> findAll(int page, int size) {
		Page<BettingHouseJpaEntity> result = jpaRepository.findAll(PageRequest.of(page, size));
		return new PagedResult<>(result.getContent().stream().map(JpaBettingHouseRepository::toDomain).toList(),
				result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
	}

	private static BettingHouse toDomain(BettingHouseJpaEntity entity) {
		return new BettingHouse(entity.getId(), entity.getName(), entity.getInitialBalance(), entity.getCreatedAt());
	}
}
