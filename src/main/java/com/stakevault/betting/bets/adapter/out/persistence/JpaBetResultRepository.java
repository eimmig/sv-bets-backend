package com.stakevault.betting.bets.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.stakevault.betting.bets.domain.model.BetResult;
import com.stakevault.betting.bets.domain.port.out.BetResultRepository;

@Repository
public class JpaBetResultRepository implements BetResultRepository {

	private final BetResultSpringDataRepository jpaRepository;

	public JpaBetResultRepository(BetResultSpringDataRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public BetResult save(BetResult betResult) {
		return toDomain(jpaRepository.save(new BetResultJpaEntity(betResult)));
	}

	@Override
	public Optional<BetResult> findByBetId(UUID betId) {
		return jpaRepository.findByBetId(betId).map(JpaBetResultRepository::toDomain);
	}

	private static BetResult toDomain(BetResultJpaEntity entity) {
		return new BetResult(entity.getId(), entity.getBetId(), entity.getSettledByUserId(), entity.getProfit(),
				entity.getSettledAt());
	}
}
