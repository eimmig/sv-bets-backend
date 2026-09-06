package com.stakevault.betting.bets.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import com.stakevault.betting.bets.domain.model.Bet;
import com.stakevault.betting.bets.domain.model.BetResult;
import com.stakevault.betting.bets.domain.model.BetStatus;
import com.stakevault.betting.bets.domain.model.InvalidStatusTransitionException;
import com.stakevault.betting.bets.domain.port.in.CreateBetCommand;
import com.stakevault.betting.bets.domain.port.out.BetEventPublisher;
import com.stakevault.betting.bets.domain.port.out.BetRepository;
import com.stakevault.betting.bets.domain.port.out.BetResultRepository;
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
	private BetResultRepository betResultRepository;
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
	@Mock
	private BetEventPublisher betEventPublisher;

	private BetService service;

	private BetService service() {
		return new BetService(betRepository, betResultRepository, bettingHouseRepository, sportRepository,
				leagueRepository, marketRepository, tipsterRepository, betEventPublisher);
	}

	private Bet pendingBet(BigDecimal stake, BigDecimal odd) {
		return new Bet(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
				null, UUID.randomUUID(), null, null, null, null, null, null, stake, odd, BetStatus.PENDING,
				Instant.now(), null);
	}

	@Test
	void shouldReturnExistingBetWhenIdempotencyKeyRacesOnUniqueConstraint() {
		service = service();

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
		verify(betEventPublisher, never()).publishCreated(any());
	}

	@Test
	void shouldReturnExistingBetWithoutPublishingWhenIdempotencyKeyAlreadyExists() {
		service = service();
		Bet existing = pendingBet(BigDecimal.TEN, BigDecimal.valueOf(1.5));
		CreateBetCommand command = new CreateBetCommand(UUID.randomUUID(), existing.bettingHouseId(),
				existing.sportId(), existing.leagueId(), existing.marketId(), null, null, null, null, null, null,
				null, BigDecimal.TEN, BigDecimal.valueOf(1.5), Instant.now(), "already-used-key");
		when(betRepository.findByIdempotencyKey("already-used-key")).thenReturn(Optional.of(existing));

		var result = service.create(command);

		assertThat(result.created()).isFalse();
		verify(betEventPublisher, never()).publishCreated(any());
	}

	@Test
	void shouldPublishCreatedEventOnceForANewBet() {
		service = service();
		CreateBetCommand command = new CreateBetCommand(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
				UUID.randomUUID(), UUID.randomUUID(), null, null, null, null, null, null, null, BigDecimal.TEN,
				BigDecimal.valueOf(1.5), Instant.now(), null);
		when(bettingHouseRepository.existsById(any())).thenReturn(true);
		when(sportRepository.existsById(any())).thenReturn(true);
		when(leagueRepository.existsById(any())).thenReturn(true);
		when(marketRepository.existsById(any())).thenReturn(true);
		when(betRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		var result = service.create(command);

		assertThat(result.created()).isTrue();
		verify(betEventPublisher).publishCreated(result.bet());
	}

	@Test
	void shouldComputeProfitAsStakeTimesOddMinusStakeWhenWon() {
		service = service();
		Bet bet = pendingBet(BigDecimal.valueOf(100), BigDecimal.valueOf(2.5));
		UUID settledByUserId = UUID.randomUUID();
		when(betRepository.transitionStatus(bet.id(), BetStatus.PENDING, BetStatus.WON)).thenReturn(true);
		when(betRepository.findById(bet.id())).thenReturn(Optional.of(bet));

		service.updateStatus(bet.id(), BetStatus.WON, settledByUserId);

		ArgumentCaptor<BetResult> captor = ArgumentCaptor.forClass(BetResult.class);
		verify(betResultRepository).save(captor.capture());
		assertThat(captor.getValue().profit()).isEqualByComparingTo("150");
		assertThat(captor.getValue().settledByUserId()).isEqualTo(settledByUserId);
		assertThat(captor.getValue().betId()).isEqualTo(bet.id());
	}

	@Test
	void shouldComputeProfitAsNegativeStakeWhenLost() {
		service = service();
		Bet bet = pendingBet(BigDecimal.valueOf(100), BigDecimal.valueOf(2.5));
		when(betRepository.transitionStatus(bet.id(), BetStatus.PENDING, BetStatus.LOST)).thenReturn(true);
		when(betRepository.findById(bet.id())).thenReturn(Optional.of(bet));

		service.updateStatus(bet.id(), BetStatus.LOST, UUID.randomUUID());

		ArgumentCaptor<BetResult> captor = ArgumentCaptor.forClass(BetResult.class);
		verify(betResultRepository).save(captor.capture());
		assertThat(captor.getValue().profit()).isEqualByComparingTo("-100");
	}

	@Test
	void shouldComputeZeroProfitWhenVoid() {
		service = service();
		Bet bet = pendingBet(BigDecimal.valueOf(100), BigDecimal.valueOf(2.5));
		when(betRepository.transitionStatus(bet.id(), BetStatus.PENDING, BetStatus.VOID)).thenReturn(true);
		when(betRepository.findById(bet.id())).thenReturn(Optional.of(bet));

		service.updateStatus(bet.id(), BetStatus.VOID, UUID.randomUUID());

		ArgumentCaptor<BetResult> captor = ArgumentCaptor.forClass(BetResult.class);
		verify(betResultRepository).save(captor.capture());
		assertThat(captor.getValue().profit()).isEqualByComparingTo("0");
	}

	@Test
	void shouldRejectTransitionWithoutSavingResultWhenAtomicUpdateLosesTheRace() {
		service = service();
		Bet bet = pendingBet(BigDecimal.TEN, BigDecimal.valueOf(2));
		Bet alreadySettled = new Bet(bet.id(), bet.bettingHouseId(), bet.sportId(), bet.leagueId(), bet.marketId(),
				null, bet.createdByUserId(), null, null, null, null, null, null, bet.stake(), bet.odd(),
				BetStatus.WON, bet.betDate(), null);
		UUID betId = bet.id();
		UUID settledByUserId = UUID.randomUUID();
		when(betRepository.transitionStatus(betId, BetStatus.PENDING, BetStatus.LOST)).thenReturn(false);
		when(betRepository.findById(betId)).thenReturn(Optional.of(alreadySettled));

		assertThatThrownBy(() -> service.updateStatus(betId, BetStatus.LOST, settledByUserId))
				.isInstanceOf(InvalidStatusTransitionException.class);

		verify(betResultRepository, never()).save(any());
		verify(betEventPublisher, never()).publishSettled(any(), any());
	}

	@Test
	void shouldPublishSettledEventOnceForASuccessfulSettlement() {
		service = service();
		Bet bet = pendingBet(BigDecimal.valueOf(100), BigDecimal.valueOf(2.5));
		UUID settledByUserId = UUID.randomUUID();
		when(betRepository.transitionStatus(bet.id(), BetStatus.PENDING, BetStatus.WON)).thenReturn(true);
		when(betRepository.findById(bet.id())).thenReturn(Optional.of(bet));
		when(betResultRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		service.updateStatus(bet.id(), BetStatus.WON, settledByUserId);

		ArgumentCaptor<BetResult> captor = ArgumentCaptor.forClass(BetResult.class);
		verify(betEventPublisher).publishSettled(eq(bet), captor.capture());
		assertThat(captor.getValue().settledByUserId()).isEqualTo(settledByUserId);
	}

	@Test
	void shouldRejectTransitionToPendingWithoutCallingRepository() {
		service = service();
		Bet bet = pendingBet(BigDecimal.TEN, BigDecimal.valueOf(2));
		UUID betId = bet.id();
		UUID settledByUserId = UUID.randomUUID();
		when(betRepository.findById(betId)).thenReturn(Optional.of(bet));

		assertThatThrownBy(() -> service.updateStatus(betId, BetStatus.PENDING, settledByUserId))
				.isInstanceOf(InvalidStatusTransitionException.class);

		verify(betRepository, never()).transitionStatus(any(), any(), any());
	}
}
