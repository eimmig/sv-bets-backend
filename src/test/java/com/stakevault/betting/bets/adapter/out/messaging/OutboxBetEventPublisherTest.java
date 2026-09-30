package com.stakevault.betting.bets.adapter.out.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.stakevault.betting.bets.config.TenantContextScope;
import com.stakevault.betting.bets.domain.model.Bet;
import com.stakevault.betting.bets.domain.model.BetDimensionNames;
import com.stakevault.betting.bets.domain.model.BetResult;
import com.stakevault.betting.bets.domain.model.BetStatus;
import com.stakevault.betting.bets.domain.model.TenantSchemaName;

import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class OutboxBetEventPublisherTest {

	@Mock
	private OutboxEventSpringDataRepository repository;

	private OutboxBetEventPublisher publisher;
	private Bet bet;
	private BetDimensionNames names;

	@BeforeEach
	void setUp() {
		publisher = new OutboxBetEventPublisher(repository, JsonMapper.builder().findAndAddModules().build());
		bet = new Bet(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
				null, UUID.randomUUID(), null, null, null, null, null, null, BigDecimal.TEN, BigDecimal.valueOf(2),
				BetStatus.WON, Instant.now(), null);
		names = new BetDimensionNames("House", "Sport", "League", "Market", null, null, null);
	}

	@Test
	void shouldStoreTheCreatedEventWithRouteTenantAndBetId() {
		try (var _ = TenantContextScope.open(TenantSchemaName.fromSlug("acme"))) {
			publisher.publishCreated(bet, names);
		}

		ArgumentCaptor<OutboxEventJpaEntity> captor = ArgumentCaptor.forClass(OutboxEventJpaEntity.class);
		verify(repository).save(captor.capture());
		OutboxEventJpaEntity stored = captor.getValue();
		assertThat(stored.getRoutingKey()).isEqualTo("bet.created");
		assertThat(stored.getTenantSlug()).isEqualTo("acme");
		assertThat(stored.getBetId()).isEqualTo(bet.id());
		assertThat(stored.getPayload()).contains("\"eventType\":\"BetCreated\"");
	}

	@Test
	void shouldStoreTheSettledEventWithItsOwnRoute() {
		BetResult result = new BetResult(UUID.randomUUID(), bet.id(), UUID.randomUUID(), BigDecimal.TEN,
				Instant.now());

		try (var _ = TenantContextScope.open(TenantSchemaName.fromSlug("acme"))) {
			publisher.publishSettled(bet, result, names);
		}

		ArgumentCaptor<OutboxEventJpaEntity> captor = ArgumentCaptor.forClass(OutboxEventJpaEntity.class);
		verify(repository).save(captor.capture());
		assertThat(captor.getValue().getRoutingKey()).isEqualTo("bet.settled");
		assertThat(captor.getValue().getPayload()).contains("\"eventType\":\"BetSettled\"");
	}

	@Test
	void shouldNotSwallowAStorageFailure() {
		when(repository.save(any())).thenThrow(new IllegalStateException("database unavailable"));

		try (var _ = TenantContextScope.open(TenantSchemaName.fromSlug("acme"))) {
			assertThatThrownBy(() -> publisher.publishCreated(bet, names)).isInstanceOf(IllegalStateException.class)
					.hasMessage("database unavailable");
		}
	}
}
