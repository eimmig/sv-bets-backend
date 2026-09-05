package com.stakevault.betting.bets.adapter.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.stakevault.betting.bets.domain.model.League;
import com.stakevault.betting.bets.domain.model.PagedResult;
import com.stakevault.betting.bets.domain.port.out.LeagueRepository;

@Repository
public class JpaLeagueRepository implements LeagueRepository {

	private final LeagueSpringDataRepository jpaRepository;

	public JpaLeagueRepository(LeagueSpringDataRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public League save(League league) {
		LeagueJpaEntity saved = jpaRepository.save(new LeagueJpaEntity(league.id(), league.name()));
		return toDomain(saved);
	}

	@Override
	public boolean existsByName(String name) {
		return jpaRepository.existsByName(name);
	}

	@Override
	public PagedResult<League> findAll(int page, int size) {
		Page<LeagueJpaEntity> result = jpaRepository.findAll(PageRequest.of(page, size));
		return new PagedResult<>(result.getContent().stream().map(JpaLeagueRepository::toDomain).toList(),
				result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
	}

	private static League toDomain(LeagueJpaEntity entity) {
		return new League(entity.getId(), entity.getName());
	}
}
