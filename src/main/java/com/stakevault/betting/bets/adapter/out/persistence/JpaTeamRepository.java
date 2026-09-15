package com.stakevault.betting.bets.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.stakevault.betting.bets.domain.model.PagedResult;
import com.stakevault.betting.bets.domain.model.Team;
import com.stakevault.betting.bets.domain.port.out.TeamRepository;

@Repository
public class JpaTeamRepository implements TeamRepository {

	private final TeamSpringDataRepository jpaRepository;

	public JpaTeamRepository(TeamSpringDataRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public Team save(Team team) {
		TeamJpaEntity saved = jpaRepository.save(new TeamJpaEntity(team.id(), team.name(), team.sportId()));
		return toDomain(saved);
	}

	@Override
	public boolean existsByNameAndSportId(String name, UUID sportId) {
		return jpaRepository.existsByNameAndSportId(name, sportId);
	}

	@Override
	public boolean existsById(UUID id) {
		return jpaRepository.existsById(id);
	}

	@Override
	public Optional<Team> findById(UUID id) {
		return jpaRepository.findById(id).map(JpaTeamRepository::toDomain);
	}

	@Override
	public PagedResult<Team> findAll(int page, int size) {
		Page<TeamJpaEntity> result = jpaRepository.findAll(PageRequest.of(page, size));
		return new PagedResult<>(result.getContent().stream().map(JpaTeamRepository::toDomain).toList(),
				result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
	}

	private static Team toDomain(TeamJpaEntity entity) {
		return new Team(entity.getId(), entity.getName(), entity.getSportId());
	}
}
