package com.stakevault.betting.bets.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import com.stakevault.betting.bets.config.TenantContextScope;
import com.stakevault.betting.bets.domain.model.League;
import com.stakevault.betting.bets.domain.model.PagedResult;
import com.stakevault.betting.bets.domain.port.in.ProvisionTenantSchemaUseCase;
import com.stakevault.betting.bets.domain.port.out.LeagueRepository;
import com.stakevault.betting.bets.support.TenantSchemaIntegrationSupport;

class JpaLeagueRepositoryIntegrationTest extends TenantSchemaIntegrationSupport {

	private final LeagueRepository leagueRepository;

	JpaLeagueRepositoryIntegrationTest(ProvisionTenantSchemaUseCase provisionTenantSchema, JdbcTemplate jdbcTemplate,
			LeagueRepository leagueRepository) {
		super(provisionTenantSchema, jdbcTemplate);
		this.leagueRepository = leagueRepository;
	}

	@Test
	void shouldSaveAndListInSamePage() {
		League league = new League(UUID.randomUUID(), "Brasileirao");

		try (var _ = TenantContextScope.open(schema)) {
			leagueRepository.save(league);

			PagedResult<League> page = leagueRepository.findAll(0, 20);

			assertThat(page.content()).contains(league);
			assertThat(page.totalElements()).isEqualTo(1);
		}
	}

	@Test
	void existsByNameShouldReflectSavedRows() {
		try (var _ = TenantContextScope.open(schema)) {
			assertThat(leagueRepository.existsByName("Premier League")).isFalse();

			leagueRepository.save(new League(UUID.randomUUID(), "Premier League"));

			assertThat(leagueRepository.existsByName("Premier League")).isTrue();
		}
	}
}
