package com.stakevault.betting.bets.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stakevault.betting.bets.domain.model.Bet;
import com.stakevault.betting.bets.domain.model.BetDimensionNames;
import com.stakevault.betting.bets.domain.model.BetFilter;
import com.stakevault.betting.bets.domain.model.BetNotFoundException;
import com.stakevault.betting.bets.domain.model.BetResult;
import com.stakevault.betting.bets.domain.model.BetStatus;
import com.stakevault.betting.bets.domain.model.BettingHouseNotFoundException;
import com.stakevault.betting.bets.domain.model.PagedResult;
import com.stakevault.betting.bets.domain.model.InvalidOddException;
import com.stakevault.betting.bets.domain.model.InvalidStakeException;
import com.stakevault.betting.bets.domain.model.InvalidStatusTransitionException;
import com.stakevault.betting.bets.domain.model.LeagueNotFoundException;
import com.stakevault.betting.bets.domain.model.MarketNotFoundException;
import com.stakevault.betting.bets.domain.model.SportNotFoundException;
import com.stakevault.betting.bets.domain.model.TeamNotFoundException;
import com.stakevault.betting.bets.domain.model.TipsterNotFoundException;
import com.stakevault.betting.bets.domain.model.BetConcurrentlyModifiedException;
import com.stakevault.betting.bets.domain.port.in.BetCreationResult;
import com.stakevault.betting.bets.domain.port.in.BetDetails;
import com.stakevault.betting.bets.domain.port.in.BetUseCase;
import com.stakevault.betting.bets.domain.port.in.CreateBetCommand;
import com.stakevault.betting.bets.domain.port.in.UpdateBetCommand;
import com.stakevault.betting.bets.domain.port.out.BetEventPublisher;
import com.stakevault.betting.bets.domain.port.out.BetRepository;
import com.stakevault.betting.bets.domain.port.out.BetResultRepository;
import com.stakevault.betting.bets.domain.port.out.BettingHouseRepository;
import com.stakevault.betting.bets.domain.port.out.LeagueRepository;
import com.stakevault.betting.bets.domain.port.out.MarketRepository;
import com.stakevault.betting.bets.domain.port.out.SportRepository;
import com.stakevault.betting.bets.domain.port.out.TeamRepository;
import com.stakevault.betting.bets.domain.port.out.TipsterRepository;

@Service
public class BetService implements BetUseCase {

	private final BetRepository betRepository;
	private final BetResultRepository betResultRepository;
	private final BettingHouseRepository bettingHouseRepository;
	private final SportRepository sportRepository;
	private final LeagueRepository leagueRepository;
	private final MarketRepository marketRepository;
	private final TipsterRepository tipsterRepository;
	private final TeamRepository teamRepository;
	private final BetEventPublisher betEventPublisher;

	public BetService(BetRepository betRepository, BetResultRepository betResultRepository,
			BettingHouseRepository bettingHouseRepository, SportRepository sportRepository,
			LeagueRepository leagueRepository, MarketRepository marketRepository, TipsterRepository tipsterRepository,
			TeamRepository teamRepository, BetEventPublisher betEventPublisher) {
		this.betRepository = betRepository;
		this.betResultRepository = betResultRepository;
		this.bettingHouseRepository = bettingHouseRepository;
		this.sportRepository = sportRepository;
		this.leagueRepository = leagueRepository;
		this.marketRepository = marketRepository;
		this.tipsterRepository = tipsterRepository;
		this.teamRepository = teamRepository;
		this.betEventPublisher = betEventPublisher;
	}

	@Override
	public BetCreationResult create(CreateBetCommand command) {
		if (command.idempotencyKey() != null) {
			var existing = betRepository.findByIdempotencyKey(command.idempotencyKey());
			if (existing.isPresent()) {
				return new BetCreationResult(existing.get(), false);
			}
		}

		BetDetails d = command.details();
		validateReferences(d.bettingHouseId(), d.sportId(), d.leagueId(), d.marketId(), d.tipsterId(), d.team1Id(),
				d.team2Id());
		validateBusinessRules(d.stake(), d.odd());

		Bet bet = new Bet(UUID.randomUUID(), d.bettingHouseId(), d.sportId(), d.leagueId(), d.marketId(),
				d.tipsterId(), command.callerId(), d.ticketNumber(), d.team1Id(), d.team2Id(), d.description(),
				d.betType(), d.playType(), d.stake(), d.odd(), BetStatus.PENDING,
				d.betDate() == null ? Instant.now() : d.betDate(), command.idempotencyKey());

		try {
			Bet saved = betRepository.save(bet);
			betEventPublisher.publishCreated(saved, resolveDimensionNames(saved));
			return new BetCreationResult(saved, true);
		} catch (DataIntegrityViolationException _) {
			// Concurrent replay of the same Idempotency-Key raced us to the unique constraint.
			return betRepository.findByIdempotencyKey(command.idempotencyKey())
					.map(found -> new BetCreationResult(found, false))
					.orElseThrow();
		}
	}

	@Override
	public Bet findById(UUID id) {
		return betRepository.findById(id).orElseThrow(() -> new BetNotFoundException(id));
	}

	@Override
	@Transactional
	public Bet updateStatus(UUID id, BetStatus newStatus, UUID settledByUserId) {
		if (newStatus == BetStatus.PENDING || !betRepository.transitionStatus(id, BetStatus.PENDING, newStatus)) {
			Bet current = findById(id);
			throw new InvalidStatusTransitionException(current.status(), newStatus);
		}

		Bet settled = findById(id);
		BigDecimal profit = computeProfit(settled, newStatus);
		BetResult result = betResultRepository.save(new BetResult(UUID.randomUUID(), id, settledByUserId, profit,
				Instant.now()));
		betEventPublisher.publishSettled(settled, result, resolveDimensionNames(settled));
		return settled;
	}

	@Override
	@Transactional
	public Bet update(UUID id, UpdateBetCommand command) {
		Bet current = findById(id);

		BetDetails d = command.details();
		validateReferences(d.bettingHouseId(), d.sportId(), d.leagueId(), d.marketId(), d.tipsterId(), d.team1Id(),
				d.team2Id());
		validateBusinessRules(d.stake(), d.odd());

		boolean wasPending = current.status() == BetStatus.PENDING;
		boolean staysPending = command.status() == BetStatus.PENDING;
		if (wasPending != staysPending) {
			throw new InvalidStatusTransitionException(current.status(), command.status());
		}

		Bet updated = new Bet(id, d.bettingHouseId(), d.sportId(), d.leagueId(), d.marketId(), d.tipsterId(),
				current.createdByUserId(), d.ticketNumber(), d.team1Id(), d.team2Id(), d.description(), d.betType(),
				d.playType(), d.stake(), d.odd(), command.status(), d.betDate(), current.idempotencyKey());

		if (betRepository.updateFields(updated, current.status()) == 0) {
			throw new BetConcurrentlyModifiedException(id);
		}

		if (staysPending) {
			betEventPublisher.publishCreated(updated, resolveDimensionNames(updated));
			return updated;
		}

		BetResult existingResult = betResultRepository.findByBetId(id).orElseThrow();
		BigDecimal profit = computeProfit(updated, updated.status());
		betResultRepository.updateProfit(id, profit);
		BetResult updatedResult = new BetResult(existingResult.id(), id, existingResult.settledByUserId(), profit,
				existingResult.settledAt());
		betEventPublisher.publishSettled(updated, updatedResult, resolveDimensionNames(updated));
		return updated;
	}

	@Override
	public PagedResult<Bet> list(BetFilter filter, int page, int size) {
		return betRepository.findFiltered(filter, page, size);
	}

	private BigDecimal computeProfit(Bet bet, BetStatus status) {
		return switch (status) {
			case WON -> bet.stake().multiply(bet.odd()).subtract(bet.stake());
			case LOST -> bet.stake().negate();
			case VOID -> BigDecimal.ZERO;
			case PENDING -> throw new IllegalStateException("pending is not a settlement status");
		};
	}

	private BetDimensionNames resolveDimensionNames(Bet bet) {
		String bettingHouseName = bettingHouseRepository.findById(bet.bettingHouseId())
				.orElseThrow(() -> new BettingHouseNotFoundException(bet.bettingHouseId())).name();
		String sportName = sportRepository.findById(bet.sportId())
				.orElseThrow(() -> new SportNotFoundException(bet.sportId())).name();
		String leagueName = leagueRepository.findById(bet.leagueId())
				.orElseThrow(() -> new LeagueNotFoundException(bet.leagueId())).name();
		String marketName = marketRepository.findById(bet.marketId())
				.orElseThrow(() -> new MarketNotFoundException(bet.marketId())).name();
		String tipsterName = bet.tipsterId() == null ? null : tipsterRepository.findById(bet.tipsterId())
				.orElseThrow(() -> new TipsterNotFoundException(bet.tipsterId())).name();
		String team1Name = bet.team1Id() == null ? null : teamRepository.findById(bet.team1Id())
				.orElseThrow(() -> new TeamNotFoundException(bet.team1Id())).name();
		String team2Name = bet.team2Id() == null ? null : teamRepository.findById(bet.team2Id())
				.orElseThrow(() -> new TeamNotFoundException(bet.team2Id())).name();
		return new BetDimensionNames(bettingHouseName, sportName, leagueName, marketName, tipsterName, team1Name,
				team2Name);
	}

	private void validateReferences(UUID bettingHouseId, UUID sportId, UUID leagueId, UUID marketId, UUID tipsterId,
			UUID team1Id, UUID team2Id) {
		if (!bettingHouseRepository.existsById(bettingHouseId)) {
			throw new BettingHouseNotFoundException(bettingHouseId);
		}
		if (!sportRepository.existsById(sportId)) {
			throw new SportNotFoundException(sportId);
		}
		if (!leagueRepository.existsById(leagueId)) {
			throw new LeagueNotFoundException(leagueId);
		}
		if (!marketRepository.existsById(marketId)) {
			throw new MarketNotFoundException(marketId);
		}
		if (tipsterId != null && !tipsterRepository.existsById(tipsterId)) {
			throw new TipsterNotFoundException(tipsterId);
		}
		if (team1Id != null && !teamRepository.existsById(team1Id)) {
			throw new TeamNotFoundException(team1Id);
		}
		if (team2Id != null && !teamRepository.existsById(team2Id)) {
			throw new TeamNotFoundException(team2Id);
		}
	}

	private void validateBusinessRules(BigDecimal stake, BigDecimal odd) {
		if (stake == null || stake.signum() <= 0) {
			throw new InvalidStakeException(stake);
		}
		if (odd == null || odd.compareTo(BigDecimal.ONE) <= 0) {
			throw new InvalidOddException(odd);
		}
	}
}
