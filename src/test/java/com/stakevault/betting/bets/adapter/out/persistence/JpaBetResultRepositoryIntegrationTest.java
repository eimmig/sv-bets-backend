package com.stakevault.betting.bets.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import com.stakevault.betting.bets.config.TenantContextScope;
import com.stakevault.betting.bets.domain.model.Bet;
import com.stakevault.betting.bets.domain.model.BetResult;
import com.stakevault.betting.bets.domain.model.BetStatus;
import com.stakevault.betting.bets.domain.model.BettingHouse;
import com.stakevault.betting.bets.domain.model.League;
import com.stakevault.betting.bets.domain.model.Market;
import com.stakevault.betting.bets.domain.model.Sport;
import com.stakevault.betting.bets.domain.port.in.ProvisionTenantSchemaUseCase;
import com.stakevault.betting.bets.domain.port.out.BetRepository;
import com.stakevault.betting.bets.domain.port.out.BetResultRepository;
import com.stakevault.betting.bets.domain.port.out.BettingHouseRepository;
import com.stakevault.betting.bets.domain.port.out.LeagueRepository;
import com.stakevault.betting.bets.domain.port.out.MarketRepository;
import com.stakevault.betting.bets.domain.port.out.SportRepository;
import com.stakevault.betting.bets.support.TenantSchemaIntegrationSupport;

class JpaBetResultRepositoryIntegrationTest extends TenantSchemaIntegrationSupport {

	private final BetResultRepository betResultRepository;
	private final BetRepository betRepository;
	private final BettingHouseRepository bettingHouseRepository;
	private final SportRepository sportRepository;
	private final LeagueRepository leagueRepository;
	private final MarketRepository marketRepository;

	JpaBetResultRepositoryIntegrationTest(ProvisionTenantSchemaUseCase provisionTenantSchema, JdbcTemplate jdbcTemplate,
			BetResultRepository betResultRepository, BetRepository betRepository,
			BettingHouseRepository bettingHouseRepository, SportRepository sportRepository,
			LeagueRepository leagueRepository, MarketRepository marketRepository) {
		super(provisionTenantSchema, jdbcTemplate);
		this.betResultRepository = betResultRepository;
		this.betRepository = betRepository;
		this.bettingHouseRepository = bettingHouseRepository;
		this.sportRepository = sportRepository;
		this.leagueRepository = leagueRepository;
		this.marketRepository = marketRepository;
	}

	private UUID newBetId() {
		UUID bettingHouseId = bettingHouseRepository
				.save(new BettingHouse(UUID.randomUUID(), "House-" + UUID.randomUUID(), BigDecimal.ZERO, Instant.now()))
				.id();
		UUID sportId = sportRepository.save(new Sport(UUID.randomUUID(), "Sport-" + UUID.randomUUID())).id();
		UUID leagueId = leagueRepository.save(new League(UUID.randomUUID(), "League-" + UUID.randomUUID())).id();
		UUID marketId = marketRepository.save(new Market(UUID.randomUUID(), "Market-" + UUID.randomUUID())).id();
		Bet bet = new Bet(UUID.randomUUID(), bettingHouseId, sportId, leagueId, marketId, null, UUID.randomUUID(), null,
				null, null, null, null, null, BigDecimal.TEN, BigDecimal.valueOf(2), BetStatus.PENDING, Instant.now(),
				null);
		return betRepository.save(bet).id();
	}

	@Test
	void shouldSaveAndFindByBetId() {
		try (var _ = TenantContextScope.open(schema)) {
			UUID betId = newBetId();
			BetResult result = new BetResult(UUID.randomUUID(), betId, UUID.randomUUID(), BigDecimal.valueOf(10),
					Instant.now());

			betResultRepository.save(result);

			BetResult found = betResultRepository.findByBetId(betId).orElseThrow();
			assertThat(found.betId()).isEqualTo(betId);
			assertThat(found.profit()).isEqualByComparingTo("10");
		}
	}

	@Test
	void shouldRejectSecondResultForSameBet() {
		try (var _ = TenantContextScope.open(schema)) {
			UUID betId = newBetId();
			betResultRepository.save(new BetResult(UUID.randomUUID(), betId, UUID.randomUUID(), BigDecimal.TEN,
					Instant.now()));

			assertThatThrownBy(() -> betResultRepository.save(
					new BetResult(UUID.randomUUID(), betId, UUID.randomUUID(), BigDecimal.ONE, Instant.now())))
					.isInstanceOf(DataIntegrityViolationException.class);
		}
	}

	@Test
	void betResultsShouldBeIsolatedBetweenTenants() {
		UUID betId;
		try (var _ = TenantContextScope.open(schema)) {
			betId = newBetId();
			betResultRepository.save(new BetResult(UUID.randomUUID(), betId, UUID.randomUUID(), BigDecimal.TEN,
					Instant.now()));
		}

		String otherSlug = "test-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
		provisionTenantSchema.ensureSchemaExists(otherSlug);
		var otherSchema = com.stakevault.betting.bets.domain.model.TenantSchemaName.fromSlug(otherSlug);
		try (var _ = TenantContextScope.open(otherSchema)) {
			assertThat(betResultRepository.findByBetId(betId)).isEmpty();
		} finally {
			jdbcTemplate.execute("DROP SCHEMA IF EXISTS \"" + otherSchema.value() + "\" CASCADE");
		}
	}
}
