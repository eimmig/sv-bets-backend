package com.stakevault.betting.bets.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import com.stakevault.betting.bets.config.TenantContextScope;
import com.stakevault.betting.bets.domain.model.PagedResult;
import com.stakevault.betting.bets.domain.model.Sport;
import com.stakevault.betting.bets.domain.model.Team;
import com.stakevault.betting.bets.domain.port.in.ProvisionTenantSchemaUseCase;
import com.stakevault.betting.bets.domain.port.out.SportRepository;
import com.stakevault.betting.bets.domain.port.out.TeamRepository;
import com.stakevault.betting.bets.support.TenantSchemaIntegrationSupport;

class JpaTeamRepositoryIntegrationTest extends TenantSchemaIntegrationSupport {

	private final TeamRepository teamRepository;
	private final SportRepository sportRepository;

	JpaTeamRepositoryIntegrationTest(ProvisionTenantSchemaUseCase provisionTenantSchema, JdbcTemplate jdbcTemplate,
			TeamRepository teamRepository, SportRepository sportRepository) {
		super(provisionTenantSchema, jdbcTemplate);
		this.teamRepository = teamRepository;
		this.sportRepository = sportRepository;
	}

	@Test
	void shouldSaveAndListInSamePage() {
		try (var _ = TenantContextScope.open(schema)) {
			Sport sport = sportRepository.save(new Sport(UUID.randomUUID(), "CS"));
			Team team = new Team(UUID.randomUUID(), "Furia", sport.id());

			teamRepository.save(team);

			PagedResult<Team> page = teamRepository.findAll(0, 20);

			assertThat(page.content()).contains(team);
			assertThat(page.totalElements()).isEqualTo(1);
		}
	}

	@Test
	void existsByNameAndSportIdShouldReflectSavedRows() {
		try (var _ = TenantContextScope.open(schema)) {
			Sport sport = sportRepository.save(new Sport(UUID.randomUUID(), "LoL"));

			assertThat(teamRepository.existsByNameAndSportId("Furia", sport.id())).isFalse();

			teamRepository.save(new Team(UUID.randomUUID(), "Furia", sport.id()));

			assertThat(teamRepository.existsByNameAndSportId("Furia", sport.id())).isTrue();
		}
	}

	@Test
	void sameNameShouldBeAllowedAcrossDifferentSports() {
		try (var _ = TenantContextScope.open(schema)) {
			Sport cs = sportRepository.save(new Sport(UUID.randomUUID(), "CS2"));
			Sport lol = sportRepository.save(new Sport(UUID.randomUUID(), "LoL2"));

			teamRepository.save(new Team(UUID.randomUUID(), "Furia", cs.id()));
			teamRepository.save(new Team(UUID.randomUUID(), "Furia", lol.id()));

			assertThat(teamRepository.existsByNameAndSportId("Furia", cs.id())).isTrue();
			assertThat(teamRepository.existsByNameAndSportId("Furia", lol.id())).isTrue();
			assertThat(teamRepository.findAll(0, 20).totalElements()).isEqualTo(2);
		}
	}
}
