package com.stakevault.betting.bets.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import com.stakevault.betting.bets.config.TenantContextScope;
import com.stakevault.betting.bets.domain.model.PagedResult;
import com.stakevault.betting.bets.domain.model.Sport;
import com.stakevault.betting.bets.domain.model.TenantSchemaName;
import com.stakevault.betting.bets.domain.port.in.ProvisionTenantSchemaUseCase;
import com.stakevault.betting.bets.domain.port.out.SportRepository;
import com.stakevault.betting.bets.support.TenantSchemaIntegrationSupport;

class JpaSportRepositoryIntegrationTest extends TenantSchemaIntegrationSupport {

	private final SportRepository sportRepository;

	JpaSportRepositoryIntegrationTest(ProvisionTenantSchemaUseCase provisionTenantSchema, JdbcTemplate jdbcTemplate,
			SportRepository sportRepository) {
		super(provisionTenantSchema, jdbcTemplate);
		this.sportRepository = sportRepository;
	}

	@Test
	void shouldSaveAndListInSamePage() {
		Sport sport = new Sport(UUID.randomUUID(), "Futebol");

		try (var _ = TenantContextScope.open(schema)) {
			sportRepository.save(sport);

			PagedResult<Sport> page = sportRepository.findAll(0, 20);

			assertThat(page.content()).contains(sport);
			assertThat(page.totalElements()).isEqualTo(1);
		}
	}

	@Test
	void existsByNameShouldReflectSavedRows() {
		try (var _ = TenantContextScope.open(schema)) {
			assertThat(sportRepository.existsByName("Basquete")).isFalse();

			sportRepository.save(new Sport(UUID.randomUUID(), "Basquete"));

			assertThat(sportRepository.existsByName("Basquete")).isTrue();
		}
	}

	@Test
	void shouldIsolateRowsBetweenTenantSchemas() {
		String otherSlug = "test-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
		TenantSchemaName otherSchema = TenantSchemaName.fromSlug(otherSlug);
		provisionTenantSchema.ensureSchemaExists(otherSlug);

		try {
			Sport inFirstTenant = new Sport(UUID.randomUUID(), "Volei");
			Sport inOtherTenant = new Sport(UUID.randomUUID(), "Volei");

			try (var _ = TenantContextScope.open(schema)) {
				sportRepository.save(inFirstTenant);
			}
			try (var _ = TenantContextScope.open(otherSchema)) {
				sportRepository.save(inOtherTenant);
			}

			PagedResult<Sport> firstTenantPage;
			PagedResult<Sport> otherTenantPage;
			try (var _ = TenantContextScope.open(schema)) {
				firstTenantPage = sportRepository.findAll(0, 20);
			}
			try (var _ = TenantContextScope.open(otherSchema)) {
				otherTenantPage = sportRepository.findAll(0, 20);
			}

			assertThat(firstTenantPage.content()).containsExactly(inFirstTenant);
			assertThat(otherTenantPage.content()).containsExactly(inOtherTenant);
		} finally {
			jdbcTemplate.execute("DROP SCHEMA IF EXISTS \"" + otherSchema.value() + "\" CASCADE");
		}
	}
}
