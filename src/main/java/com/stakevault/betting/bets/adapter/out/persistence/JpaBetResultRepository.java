package com.stakevault.betting.bets.adapter.out.persistence;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
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

	@Override
	public Map<UUID, BigDecimal> sumProfitByBettingHouseIds(Collection<UUID> bettingHouseIds) {
		if (bettingHouseIds.isEmpty()) {
			return Map.of();
		}
		Map<UUID, BigDecimal> profitById = new HashMap<>();
		for (Object[] row : jpaRepository.sumProfitByBettingHouseIds(bettingHouseIds)) {
			profitById.put((UUID) row[0], (BigDecimal) row[1]);
		}
		return profitById;
	}

	private static BetResult toDomain(BetResultJpaEntity entity) {
		return new BetResult(entity.getId(), entity.getBetId(), entity.getSettledByUserId(), entity.getProfit(),
				entity.getSettledAt());
	}
}
