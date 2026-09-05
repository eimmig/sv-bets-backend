package com.stakevault.betting.bets.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import com.stakevault.betting.bets.domain.model.Bet;
import com.stakevault.betting.bets.domain.model.BetStatus;
import com.stakevault.betting.bets.domain.port.in.CreateBetCommand;
import com.stakevault.betting.bets.domain.port.out.BetRepository;
import com.stakevault.betting.bets.domain.port.out.BettingHouseRepository;
import com.stakevault.betting.bets.domain.port.out.LeagueRepository;
import com.stakevault.betting.bets.domain.port.out.MarketRepository;
import com.stakevault.betting.bets.domain.port.out.SportRepository;
import com.stakevault.betting.bets.domain.port.out.TipsterRepository;

@ExtendWith(MockitoExtension.class)
class BetServiceTest {

	@Mock
	private BetRepository betRepository;
	@Mock
	private BettingHouseRepository bettingHouseRepository;
	@Mock
	private SportRepository sportRepository;
	@Mock
	private LeagueRepository leagueRepository;
	@Mock
	private MarketRepository marketRepository;
	@Mock
	private TipsterRepository tipsterRepository;

	private BetService service;

	@Test
	void shouldReturnExistingBetWhenIdempotencyKeyRacesOnUniqueConstraint() {
		service = new BetService(betRepository, bettingHouseRepository, sportRepository, leagueRepository,
				marketRepository, tipsterRepository);

		Bet existing = new Bet(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
				UUID.randomUUID(), null, UUID.randomUUID(), null, null, null, null, null, null, BigDecimal.TEN,
				BigDecimal.valueOf(1.5), BetStatus.PENDING, Instant.now(), "dup-key");

		CreateBetCommand command = new CreateBetCommand(UUID.randomUUID(), existing.bettingHouseId(),
				existing.sportId(), existing.leagueId(), existing.marketId(), null, null, null, null, null, null,
				null, BigDecimal.TEN, BigDecimal.valueOf(1.5), Instant.now(), "dup-key");

		when(betRepository.findByIdempotencyKey("dup-key")).thenReturn(Optional.empty(), Optional.of(existing));
		when(bettingHouseRepository.existsById(any())).thenReturn(true);
		when(sportRepository.existsById(any())).thenReturn(true);
		when(leagueRepository.existsById(any())).thenReturn(true);
		when(marketRepository.existsById(any())).thenReturn(true);
		when(betRepository.save(any())).thenThrow(new DataIntegrityViolationException("duplicate idempotency key"));

		var result = service.create(command);

		assertThat(result.created()).isFalse();
		assertThat(result.bet()).isEqualTo(existing);
	}
}
