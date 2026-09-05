package com.stakevault.betting.bets.adapter.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.stakevault.betting.bets.domain.model.PagedResult;
import com.stakevault.betting.bets.domain.model.Sport;
import com.stakevault.betting.bets.domain.port.out.SportRepository;

@Repository
public class JpaSportRepository implements SportRepository {

	private final SportSpringDataRepository jpaRepository;

	public JpaSportRepository(SportSpringDataRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public Sport save(Sport sport) {
		SportJpaEntity saved = jpaRepository.save(new SportJpaEntity(sport.id(), sport.name()));
		return toDomain(saved);
	}

	@Override
	public boolean existsByName(String name) {
		return jpaRepository.existsByName(name);
	}

	@Override
	public PagedResult<Sport> findAll(int page, int size) {
		Page<SportJpaEntity> result = jpaRepository.findAll(PageRequest.of(page, size));
		return new PagedResult<>(result.getContent().stream().map(JpaSportRepository::toDomain).toList(),
				result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
	}

	private static Sport toDomain(SportJpaEntity entity) {
		return new Sport(entity.getId(), entity.getName());
	}
}
