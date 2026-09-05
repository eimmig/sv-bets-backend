package com.stakevault.betting.bets.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.stakevault.betting.bets.domain.model.Bet;
import com.stakevault.betting.bets.domain.model.BetStatus;
import com.stakevault.betting.bets.domain.model.BettingHouseNotFoundException;
import com.stakevault.betting.bets.domain.model.InvalidOddException;
import com.stakevault.betting.bets.domain.model.InvalidStakeException;
import com.stakevault.betting.bets.domain.model.LeagueNotFoundException;
import com.stakevault.betting.bets.domain.model.MarketNotFoundException;
import com.stakevault.betting.bets.domain.model.SportNotFoundException;
import com.stakevault.betting.bets.domain.model.TipsterNotFoundException;
import com.stakevault.betting.bets.domain.port.in.BetCreationResult;
import com.stakevault.betting.bets.domain.port.in.BetUseCase;
import com.stakevault.betting.bets.domain.port.in.CreateBetCommand;
import com.stakevault.betting.bets.domain.port.out.BetRepository;
import com.stakevault.betting.bets.domain.port.out.BettingHouseRepository;
import com.stakevault.betting.bets.domain.port.out.LeagueRepository;
import com.stakevault.betting.bets.domain.port.out.MarketRepository;
import com.stakevault.betting.bets.domain.port.out.SportRepository;
import com.stakevault.betting.bets.domain.port.out.TipsterRepository;

@Service
public class BetService implements BetUseCase {

	private final BetRepository betRepository;
	private final BettingHouseRepository bettingHouseRepository;
	private final SportRepository sportRepository;
	private final LeagueRepository leagueRepository;
	private final MarketRepository marketRepository;
	private final TipsterRepository tipsterRepository;

	public BetService(BetRepository betRepository, BettingHouseRepository bettingHouseRepository,
			SportRepository sportRepository, LeagueRepository leagueRepository, MarketRepository marketRepository,
			TipsterRepository tipsterRepository) {
		this.betRepository = betRepository;
		this.bettingHouseRepository = bettingHouseRepository;
		this.sportRepository = sportRepository;
		this.leagueRepository = leagueRepository;
		this.marketRepository = marketRepository;
		this.tipsterRepository = tipsterRepository;
	}

	@Override
	public BetCreationResult create(CreateBetCommand command) {
		if (command.idempotencyKey() != null) {
			var existing = betRepository.findByIdempotencyKey(command.idempotencyKey());
			if (existing.isPresent()) {
				return new BetCreationResult(existing.get(), false);
			}
		}

		validateReferences(command);
		validateBusinessRules(command.stake(), command.odd());

		Bet bet = new Bet(UUID.randomUUID(), command.bettingHouseId(), command.sportId(), command.leagueId(),
				command.marketId(), command.tipsterId(), command.callerId(), command.ticketNumber(), command.team1(),
				command.team2(), command.description(), command.betType(), command.playType(), command.stake(),
				command.odd(), BetStatus.PENDING, command.betDate() == null ? Instant.now() : command.betDate(),
				command.idempotencyKey());

		try {
			return new BetCreationResult(betRepository.save(bet), true);
		} catch (DataIntegrityViolationException _) {
			// Concurrent replay of the same Idempotency-Key raced us to the unique constraint.
			return betRepository.findByIdempotencyKey(command.idempotencyKey())
					.map(found -> new BetCreationResult(found, false))
					.orElseThrow();
		}
	}

	private void validateReferences(CreateBetCommand command) {
		if (!bettingHouseRepository.existsById(command.bettingHouseId())) {
			throw new BettingHouseNotFoundException(command.bettingHouseId());
		}
		if (!sportRepository.existsById(command.sportId())) {
			throw new SportNotFoundException(command.sportId());
		}
		if (!leagueRepository.existsById(command.leagueId())) {
			throw new LeagueNotFoundException(command.leagueId());
		}
		if (!marketRepository.existsById(command.marketId())) {
			throw new MarketNotFoundException(command.marketId());
		}
		if (command.tipsterId() != null && !tipsterRepository.existsById(command.tipsterId())) {
			throw new TipsterNotFoundException(command.tipsterId());
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
