package com.stakevault.betting.bets.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import com.stakevault.betting.bets.config.TenantContextScope;
import com.stakevault.betting.bets.domain.model.PagedResult;
import com.stakevault.betting.bets.domain.model.Tipster;
import com.stakevault.betting.bets.domain.port.in.ProvisionTenantSchemaUseCase;
import com.stakevault.betting.bets.domain.port.out.TipsterRepository;
import com.stakevault.betting.bets.support.TenantSchemaIntegrationSupport;

class JpaTipsterRepositoryIntegrationTest extends TenantSchemaIntegrationSupport {

	private final TipsterRepository tipsterRepository;

	JpaTipsterRepositoryIntegrationTest(ProvisionTenantSchemaUseCase provisionTenantSchema, JdbcTemplate jdbcTemplate,
			TipsterRepository tipsterRepository) {
		super(provisionTenantSchema, jdbcTemplate);
		this.tipsterRepository = tipsterRepository;
	}

	@Test
	void shouldSaveAndListInSamePage() {
		Tipster tipster = new Tipster(UUID.randomUUID(), "Tio Aposta");

		try (var _ = TenantContextScope.open(schema)) {
			tipsterRepository.save(tipster);

			PagedResult<Tipster> page = tipsterRepository.findAll(0, 20);

			assertThat(page.content()).contains(tipster);
			assertThat(page.totalElements()).isEqualTo(1);
		}
	}

	@Test
	void existsByNameShouldReflectSavedRows() {
		try (var _ = TenantContextScope.open(schema)) {
			assertThat(tipsterRepository.existsByName("Fulano")).isFalse();

			tipsterRepository.save(new Tipster(UUID.randomUUID(), "Fulano"));

			assertThat(tipsterRepository.existsByName("Fulano")).isTrue();
		}
	}
}
