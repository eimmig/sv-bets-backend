package com.stakevault.betting.bets.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import com.stakevault.betting.bets.config.TenantContextScope;
import com.stakevault.betting.bets.domain.model.BettingHouse;
import com.stakevault.betting.bets.domain.model.PagedResult;
import com.stakevault.betting.bets.domain.model.TenantSchemaName;
import com.stakevault.betting.bets.domain.port.in.ProvisionTenantSchemaUseCase;
import com.stakevault.betting.bets.domain.port.out.BettingHouseRepository;
import com.stakevault.betting.bets.support.TenantSchemaIntegrationSupport;

class JpaBettingHouseRepositoryIntegrationTest extends TenantSchemaIntegrationSupport {

	private final BettingHouseRepository bettingHouseRepository;

	JpaBettingHouseRepositoryIntegrationTest(ProvisionTenantSchemaUseCase provisionTenantSchema, JdbcTemplate jdbcTemplate,
			BettingHouseRepository bettingHouseRepository) {
		super(provisionTenantSchema, jdbcTemplate);
		this.bettingHouseRepository = bettingHouseRepository;
	}

	@Test
	void shouldSaveAndListInSamePage() {
		UUID id = UUID.randomUUID();
		BettingHouse bettingHouse = new BettingHouse(id, "Bet365", BigDecimal.valueOf(100), Instant.now());

		try (var _ = TenantContextScope.open(schema)) {
			bettingHouseRepository.save(bettingHouse);

			PagedResult<BettingHouse> page = bettingHouseRepository.findAll(0, 20);

			assertThat(page.totalElements()).isEqualTo(1);
			BettingHouse persisted = page.content().stream().filter(house -> house.id().equals(id)).findFirst()
					.orElseThrow();
			assertThat(persisted.name()).isEqualTo("Bet365");
			assertThat(persisted.initialBalance()).isEqualByComparingTo("100");
		}
	}

	@Test
	void existsByNameShouldReflectSavedRows() {
		try (var _ = TenantContextScope.open(schema)) {
			assertThat(bettingHouseRepository.existsByName("Betano")).isFalse();

			bettingHouseRepository.save(new BettingHouse(UUID.randomUUID(), "Betano", BigDecimal.TEN, Instant.now()));

			assertThat(bettingHouseRepository.existsByName("Betano")).isTrue();
		}
	}

	@Test
	void existsByIdShouldReflectSavedRows() {
		UUID randomId = UUID.randomUUID();

		try (var _ = TenantContextScope.open(schema)) {
			assertThat(bettingHouseRepository.existsById(randomId)).isFalse();

			bettingHouseRepository.save(new BettingHouse(randomId, "Betfair", BigDecimal.ZERO, Instant.now()));

			assertThat(bettingHouseRepository.existsById(randomId)).isTrue();
		}
	}

	@Test
	void shouldIsolateRowsBetweenTenantSchemas() {
		String otherSlug = "test-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
		TenantSchemaName otherSchema = TenantSchemaName.fromSlug(otherSlug);
		provisionTenantSchema.ensureSchemaExists(otherSlug);

		try {
			BettingHouse inFirstTenant = new BettingHouse(UUID.randomUUID(), "KTO", BigDecimal.ZERO, Instant.now());

			try (var _ = TenantContextScope.open(schema)) {
				bettingHouseRepository.save(inFirstTenant);
			}

			try (var _ = TenantContextScope.open(otherSchema)) {
				PagedResult<BettingHouse> otherTenantPage = bettingHouseRepository.findAll(0, 20);

				assertThat(otherTenantPage.content()).isEmpty();
			}
		} finally {
			jdbcTemplate.execute("DROP SCHEMA IF EXISTS \"" + otherSchema.value() + "\" CASCADE");
		}
	}
}
