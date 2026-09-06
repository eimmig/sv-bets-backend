package com.stakevault.betting.bets.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.stakevault.betting.bets.domain.model.Bet;
import com.stakevault.betting.bets.domain.model.BetStatus;
import com.stakevault.betting.bets.domain.port.out.BetRepository;

@Repository
public class JpaBetRepository implements BetRepository {

	private final BetSpringDataRepository jpaRepository;

	public JpaBetRepository(BetSpringDataRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public Bet save(Bet bet) {
		return toDomain(jpaRepository.save(new BetJpaEntity(bet)));
	}

	@Override
	public Optional<Bet> findById(UUID id) {
		return jpaRepository.findById(id).map(JpaBetRepository::toDomain);
	}

	@Override
	public Optional<Bet> findByIdempotencyKey(String idempotencyKey) {
		return jpaRepository.findByIdempotencyKey(idempotencyKey).map(JpaBetRepository::toDomain);
	}

	@Override
	public boolean transitionStatus(UUID id, BetStatus from, BetStatus to) {
		return jpaRepository.transitionStatus(id, from, to) > 0;
	}

	private static Bet toDomain(BetJpaEntity entity) {
		return new Bet(entity.getId(), entity.getBettingHouseId(), entity.getSportId(), entity.getLeagueId(),
				entity.getMarketId(), entity.getTipsterId(), entity.getCreatedByUserId(), entity.getTicketNumber(),
				entity.getTeam1(), entity.getTeam2(), entity.getDescription(), entity.getBetType(),
				entity.getPlayType(), entity.getStake(), entity.getOdd(), entity.getStatus(), entity.getBetDate(),
				entity.getIdempotencyKey());
	}
}
