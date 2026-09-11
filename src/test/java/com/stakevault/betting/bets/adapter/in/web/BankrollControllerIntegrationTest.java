package com.stakevault.betting.bets.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

import com.stakevault.betting.bets.config.TenantContextScope;
import com.stakevault.betting.bets.domain.model.BetResult;
import com.stakevault.betting.bets.domain.model.BettingHouse;
import com.stakevault.betting.bets.domain.model.Transaction;
import com.stakevault.betting.bets.domain.model.TransactionType;
import com.stakevault.betting.bets.domain.port.in.ProvisionTenantSchemaUseCase;
import com.stakevault.betting.bets.domain.port.out.BetResultRepository;
import com.stakevault.betting.bets.domain.port.out.BetRepository;
import com.stakevault.betting.bets.domain.port.out.BettingHouseRepository;
import com.stakevault.betting.bets.domain.port.out.LeagueRepository;
import com.stakevault.betting.bets.domain.port.out.MarketRepository;
import com.stakevault.betting.bets.domain.port.out.SportRepository;
import com.stakevault.betting.bets.domain.port.out.TransactionRepository;
import com.stakevault.betting.bets.domain.model.Bet;
import com.stakevault.betting.bets.domain.model.BetStatus;
import com.stakevault.betting.bets.domain.model.League;
import com.stakevault.betting.bets.domain.model.Market;
import com.stakevault.betting.bets.domain.model.Sport;
import com.stakevault.betting.bets.support.TenantSchemaIntegrationSupport;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BankrollControllerIntegrationTest extends TenantSchemaIntegrationSupport {

	@LocalServerPort
	private int port;

	private final HttpClient httpClient = HttpClient.newHttpClient();
	private final BettingHouseRepository bettingHouseRepository;
	private final TransactionRepository transactionRepository;
	private final BetResultRepository betResultRepository;
	private final BetRepository betRepository;
	private final SportRepository sportRepository;
	private final LeagueRepository leagueRepository;
	private final MarketRepository marketRepository;

	BankrollControllerIntegrationTest(ProvisionTenantSchemaUseCase provisionTenantSchema, JdbcTemplate jdbcTemplate,
			BettingHouseRepository bettingHouseRepository, TransactionRepository transactionRepository,
			BetResultRepository betResultRepository, BetRepository betRepository, SportRepository sportRepository,
			LeagueRepository leagueRepository, MarketRepository marketRepository) {
		super(provisionTenantSchema, jdbcTemplate);
		this.bettingHouseRepository = bettingHouseRepository;
		this.transactionRepository = transactionRepository;
		this.betResultRepository = betResultRepository;
		this.betRepository = betRepository;
		this.sportRepository = sportRepository;
		this.leagueRepository = leagueRepository;
		this.marketRepository = marketRepository;
	}

	private HttpResponse<String> getBalance(String query) throws Exception {
		HttpRequest request = HttpRequest
				.newBuilder(URI.create("http://localhost:" + port + "/api/v1/bankroll/balance" + query))
				.header("X-Tenant-Id", tenantSlug)
				.GET()
				.build();
		return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
	}

	private UUID newBetId(UUID bettingHouseId) {
		UUID sportId = sportRepository.save(new Sport(UUID.randomUUID(), "Sport-" + UUID.randomUUID())).id();
		UUID leagueId = leagueRepository.save(new League(UUID.randomUUID(), "League-" + UUID.randomUUID())).id();
		UUID marketId = marketRepository.save(new Market(UUID.randomUUID(), "Market-" + UUID.randomUUID())).id();
		Bet bet = new Bet(UUID.randomUUID(), bettingHouseId, sportId, leagueId, marketId, null, UUID.randomUUID(), null,
				null, null, null, null, null, BigDecimal.TEN, BigDecimal.valueOf(2), BetStatus.PENDING, Instant.now(),
				null);
		return betRepository.save(bet).id();
	}

	@Test
	void shouldReturnCurrentBalanceWhenAtIsOmitted() throws Exception {
		try (var _ = TenantContextScope.open(schema)) {
			bettingHouseRepository
					.save(new BettingHouse(UUID.randomUUID(), "House-" + UUID.randomUUID(), BigDecimal.valueOf(300),
							Instant.now()));
		}

		HttpResponse<String> response = getBalance("");

		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.body()).contains("\"balance\":300");
	}

	@Test
	void shouldAggregateAcrossAllHousesTransactionsAndSettledBets() throws Exception {
		Instant fixedInstant = Instant.parse("2026-01-01T12:00:00Z");
		try (var _ = TenantContextScope.open(schema)) {
			UUID houseA = bettingHouseRepository
					.save(new BettingHouse(UUID.randomUUID(), "House-" + UUID.randomUUID(), BigDecimal.valueOf(100),
							Instant.now()))
					.id();
			UUID houseB = bettingHouseRepository
					.save(new BettingHouse(UUID.randomUUID(), "House-" + UUID.randomUUID(), BigDecimal.valueOf(50),
							Instant.now()))
					.id();
			transactionRepository
					.save(new Transaction(UUID.randomUUID(), houseA, TransactionType.DEPOSIT, BigDecimal.valueOf(20),
							fixedInstant));
			transactionRepository
					.save(new Transaction(UUID.randomUUID(), houseB, TransactionType.WITHDRAWAL, BigDecimal.TEN,
							fixedInstant));
			betResultRepository.save(new BetResult(UUID.randomUUID(), newBetId(houseA), UUID.randomUUID(),
					BigDecimal.valueOf(15), fixedInstant));
		}

		// 100 + 50 (initial) + 20 - 10 (transacoes) + 15 (profit) = 175
		HttpResponse<String> response = getBalance("");

		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.body()).contains("\"balance\":175");
	}

	@Test
	void shouldExcludeMovementsAfterTheRequestedDayInBrazilTimezoneEvenWhenSameUtcCalendarDayDiffers() throws Exception {
		try (var _ = TenantContextScope.open(schema)) {
			UUID houseId = bettingHouseRepository
					.save(new BettingHouse(UUID.randomUUID(), "House-" + UUID.randomUUID(), BigDecimal.ZERO,
							Instant.now()))
					.id();

			// 2026-09-11T01:00:00Z = 2026-09-10T22:00:00-03:00 - UTC calendar day is already
			// 09-11, but the Brazilian civil day is still 09-10 (America/Sao_Paulo = UTC-3).
			// Naive UTC-day comparison would wrongly exclude this from at=2026-09-10.
			transactionRepository.save(new Transaction(UUID.randomUUID(), houseId, TransactionType.DEPOSIT,
					BigDecimal.valueOf(100), Instant.parse("2026-09-11T01:00:00Z")));

			// 2026-09-11T04:00:00Z = 2026-09-11T01:00:00-03:00 - genuinely the next Brazilian
			// civil day, must be excluded from at=2026-09-10.
			transactionRepository.save(new Transaction(UUID.randomUUID(), houseId, TransactionType.DEPOSIT,
					BigDecimal.valueOf(1000), Instant.parse("2026-09-11T04:00:00Z")));
		}

		HttpResponse<String> response = getBalance("?at=2026-09-10");

		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.body()).contains("\"at\":\"2026-09-10\"").contains("\"balance\":100");
	}

	@Test
	void shouldIsolateBalanceBetweenTenantSchemas() throws Exception {
		String otherSlug = "test-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
		var otherSchema = com.stakevault.betting.bets.domain.model.TenantSchemaName.fromSlug(otherSlug);
		provisionTenantSchema.ensureSchemaExists(otherSlug);

		try {
			try (var _ = TenantContextScope.open(schema)) {
				bettingHouseRepository.save(new BettingHouse(UUID.randomUUID(), "House-" + UUID.randomUUID(),
						BigDecimal.valueOf(500), Instant.now()));
			}

			HttpRequest request = HttpRequest
					.newBuilder(URI.create("http://localhost:" + port + "/api/v1/bankroll/balance"))
					.header("X-Tenant-Id", otherSlug)
					.GET()
					.build();
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			assertThat(response.statusCode()).isEqualTo(200);
			assertThat(response.body()).contains("\"balance\":0");
		} finally {
			jdbcTemplate.execute("DROP SCHEMA IF EXISTS \"" + otherSchema.value() + "\" CASCADE");
		}
	}
}
