package com.stakevault.betting.bets.adapter.out.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import com.stakevault.betting.bets.config.TenantContextScope;
import com.stakevault.betting.bets.domain.port.in.ProvisionTenantSchemaUseCase;
import com.stakevault.betting.bets.support.TenantSchemaIntegrationSupport;

class OutboxEventPersistenceIntegrationTest extends TenantSchemaIntegrationSupport {

	private final OutboxEventSpringDataRepository repository;

	OutboxEventPersistenceIntegrationTest(ProvisionTenantSchemaUseCase provisionTenantSchema, JdbcTemplate jdbcTemplate,
			OutboxEventSpringDataRepository repository) {
		super(provisionTenantSchema, jdbcTemplate);
		this.repository = repository;
	}

	@Test
	void shouldStoreTheOutboxRowInThePublicSchemaEvenWhenSavedUnderATenantContext() {
		UUID betId = UUID.randomUUID();

		try (var _ = TenantContextScope.open(schema)) {
			OutboxEventJpaEntity saved = repository
					.save(new OutboxEventJpaEntity("bet.created", "{\"a\":1}", tenantSlug, betId, Instant.now()));

			assertThat(saved.getId()).isNotNull();
		}

		Integer inPublic = jdbcTemplate.queryForObject("SELECT count(*) FROM public.outbox_event WHERE bet_id = ?",
				Integer.class, betId);
		assertThat(inPublic).isEqualTo(1);
		String payload = jdbcTemplate.queryForObject("SELECT payload FROM public.outbox_event WHERE bet_id = ?",
				String.class, betId);
		assertThat(payload).isEqualTo("{\"a\":1}");
		jdbcTemplate.update("DELETE FROM public.outbox_event WHERE bet_id = ?", betId);
	}
}
