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
		BetJpaEntity saved = jpaRepository.save(new BetJpaEntity(bet.id(), bet.bettingHouseId(), bet.sportId(),
				bet.leagueId(), bet.marketId(), bet.tipsterId(), bet.createdByUserId(), bet.ticketNumber(),
				bet.team1(), bet.team2(), bet.description(), bet.betType(), bet.playType(), bet.stake(), bet.odd(),
				bet.status(), bet.betDate(), bet.idempotencyKey()));
		return toDomain(saved);
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
	public Bet updateStatus(UUID id, BetStatus status) {
		BetJpaEntity entity = jpaRepository.findById(id).orElseThrow(() -> new IllegalStateException("bet vanished: " + id));
		entity.updateStatus(status);
		return toDomain(jpaRepository.save(entity));
	}

	private static Bet toDomain(BetJpaEntity entity) {
		return new Bet(entity.getId(), entity.getBettingHouseId(), entity.getSportId(), entity.getLeagueId(),
				entity.getMarketId(), entity.getTipsterId(), entity.getCreatedByUserId(), entity.getTicketNumber(),
				entity.getTeam1(), entity.getTeam2(), entity.getDescription(), entity.getBetType(),
				entity.getPlayType(), entity.getStake(), entity.getOdd(), entity.getStatus(), entity.getBetDate(),
				entity.getIdempotencyKey());
	}
}
