package com.stakevault.betting.bets.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import com.stakevault.betting.bets.config.TenantContextScope;
import com.stakevault.betting.bets.domain.model.Market;
import com.stakevault.betting.bets.domain.model.PagedResult;
import com.stakevault.betting.bets.domain.port.in.ProvisionTenantSchemaUseCase;
import com.stakevault.betting.bets.domain.port.out.MarketRepository;
import com.stakevault.betting.bets.support.TenantSchemaIntegrationSupport;

class JpaMarketRepositoryIntegrationTest extends TenantSchemaIntegrationSupport {

	private final MarketRepository marketRepository;

	JpaMarketRepositoryIntegrationTest(ProvisionTenantSchemaUseCase provisionTenantSchema, JdbcTemplate jdbcTemplate,
			MarketRepository marketRepository) {
		super(provisionTenantSchema, jdbcTemplate);
		this.marketRepository = marketRepository;
	}

	@Test
	void shouldSaveAndListInSamePage() {
		Market market = new Market(UUID.randomUUID(), "Over/Under");

		try (var _ = TenantContextScope.open(schema)) {
			marketRepository.save(market);

			PagedResult<Market> page = marketRepository.findAll(0, 20);

			assertThat(page.content()).contains(market);
			assertThat(page.totalElements()).isEqualTo(1);
		}
	}

	@Test
	void existsByNameShouldReflectSavedRows() {
		try (var _ = TenantContextScope.open(schema)) {
			assertThat(marketRepository.existsByName("Handicap")).isFalse();

			marketRepository.save(new Market(UUID.randomUUID(), "Handicap"));

			assertThat(marketRepository.existsByName("Handicap")).isTrue();
		}
	}
}
