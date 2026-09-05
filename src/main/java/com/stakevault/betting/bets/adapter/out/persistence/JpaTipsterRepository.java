package com.stakevault.betting.bets.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.stakevault.betting.bets.domain.model.PagedResult;
import com.stakevault.betting.bets.domain.model.Tipster;
import com.stakevault.betting.bets.domain.port.out.TipsterRepository;

@Repository
public class JpaTipsterRepository implements TipsterRepository {

	private final TipsterSpringDataRepository jpaRepository;

	public JpaTipsterRepository(TipsterSpringDataRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public Tipster save(Tipster tipster) {
		TipsterJpaEntity saved = jpaRepository.save(new TipsterJpaEntity(tipster.id(), tipster.name()));
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
	public PagedResult<Tipster> findAll(int page, int size) {
		Page<TipsterJpaEntity> result = jpaRepository.findAll(PageRequest.of(page, size));
		return new PagedResult<>(result.getContent().stream().map(JpaTipsterRepository::toDomain).toList(),
				result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
	}

	private static Tipster toDomain(TipsterJpaEntity entity) {
		return new Tipster(entity.getId(), entity.getName());
	}
}
