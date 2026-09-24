package com.stakevault.betting.bets.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import com.stakevault.betting.bets.config.TenantContextScope;
import com.stakevault.betting.bets.domain.model.Bet;
import com.stakevault.betting.bets.domain.model.BetStatus;
import com.stakevault.betting.bets.domain.model.BetType;
import com.stakevault.betting.bets.domain.model.BettingHouse;
import com.stakevault.betting.bets.domain.model.League;
import com.stakevault.betting.bets.domain.model.Market;
import com.stakevault.betting.bets.domain.model.Sport;
import com.stakevault.betting.bets.domain.model.Team;
import com.stakevault.betting.bets.domain.model.TenantSchemaName;
import com.stakevault.betting.bets.domain.model.Tipster;
import com.stakevault.betting.bets.domain.port.in.ProvisionTenantSchemaUseCase;
import com.stakevault.betting.bets.domain.port.out.BetRepository;
import com.stakevault.betting.bets.domain.port.out.BettingHouseRepository;
import com.stakevault.betting.bets.domain.port.out.LeagueRepository;
import com.stakevault.betting.bets.domain.port.out.MarketRepository;
import com.stakevault.betting.bets.domain.port.out.SportRepository;
import com.stakevault.betting.bets.domain.port.out.TeamRepository;
import com.stakevault.betting.bets.domain.port.out.TipsterRepository;
import com.stakevault.betting.bets.support.TenantSchemaIntegrationSupport;

class JpaBetRepositoryIntegrationTest extends TenantSchemaIntegrationSupport {

	private final BetRepository betRepository;
	private final BettingHouseRepository bettingHouseRepository;
	private final SportRepository sportRepository;
	private final LeagueRepository leagueRepository;
	private final MarketRepository marketRepository;
	private final TipsterRepository tipsterRepository;
	private final TeamRepository teamRepository;

	JpaBetRepositoryIntegrationTest(ProvisionTenantSchemaUseCase provisionTenantSchema, JdbcTemplate jdbcTemplate,
			BetRepository betRepository, BettingHouseRepository bettingHouseRepository, SportRepository sportRepository,
			LeagueRepository leagueRepository, MarketRepository marketRepository, TipsterRepository tipsterRepository,
			TeamRepository teamRepository) {
		super(provisionTenantSchema, jdbcTemplate);
		this.betRepository = betRepository;
		this.bettingHouseRepository = bettingHouseRepository;
		this.sportRepository = sportRepository;
		this.leagueRepository = leagueRepository;
		this.marketRepository = marketRepository;
		this.tipsterRepository = tipsterRepository;
		this.teamRepository = teamRepository;
	}

	private UUID newBettingHouseId() {
		return bettingHouseRepository
				.save(new BettingHouse(UUID.randomUUID(), "House-" + UUID.randomUUID(), BigDecimal.ZERO, Instant.now()))
				.id();
	}

	private UUID newSportId() {
		return sportRepository.save(new Sport(UUID.randomUUID(), "Sport-" + UUID.randomUUID())).id();
	}

	private UUID newLeagueId() {
		return leagueRepository.save(new League(UUID.randomUUID(), "League-" + UUID.randomUUID())).id();
	}

	private UUID newMarketId() {
		return marketRepository.save(new Market(UUID.randomUUID(), "Market-" + UUID.randomUUID())).id();
	}

	private UUID newTipsterId() {
		return tipsterRepository.save(new Tipster(UUID.randomUUID(), "Tipster-" + UUID.randomUUID())).id();
	}

	private UUID newTeamId(UUID sportId) {
		return teamRepository.save(new Team(UUID.randomUUID(), "Team-" + UUID.randomUUID(), sportId)).id();
	}

	@Test
	void shouldSaveAndFindWithAllFieldsFilled() {
		try (var _ = TenantContextScope.open(schema)) {
			UUID sportId = newSportId();
			Bet bet = new Bet(UUID.randomUUID(), newBettingHouseId(), sportId, newLeagueId(), newMarketId(),
					newTipsterId(), UUID.randomUUID(), "TICKET-1", newTeamId(sportId), newTeamId(sportId),
					"final match", BetType.PRE, "single", BigDecimal.valueOf(100), BigDecimal.valueOf(1.5),
					BetStatus.PENDING, Instant.now(), "idem-key-1");

			betRepository.save(bet);

			Bet found = betRepository.findById(bet.id()).orElseThrow();
			assertThat(found.tipsterId()).isEqualTo(bet.tipsterId());
			assertThat(found.team1Id()).isEqualTo(bet.team1Id());
			assertThat(found.betType()).isEqualTo(BetType.PRE);
			assertThat(found.stake()).isEqualByComparingTo("100");
			assertThat(found.odd()).isEqualByComparingTo("1.5");
			assertThat(found.status()).isEqualTo(BetStatus.PENDING);
		}
	}

	@Test
	void shouldSaveAndFindWithOptionalFieldsNull() {
		try (var _ = TenantContextScope.open(schema)) {
			Bet bet = new Bet(UUID.randomUUID(), newBettingHouseId(), newSportId(), newLeagueId(), newMarketId(), null,
					UUID.randomUUID(), null, null, null, null, null, null, BigDecimal.TEN, BigDecimal.valueOf(2),
					BetStatus.PENDING, Instant.now(), null);

			betRepository.save(bet);

			Bet found = betRepository.findById(bet.id()).orElseThrow();
			assertThat(found.tipsterId()).isNull();
			assertThat(found.team1Id()).isNull();
			assertThat(found.idempotencyKey()).isNull();
		}
	}

	@Test
	void findByIdempotencyKeyShouldReturnMatchingBet() {
		try (var _ = TenantContextScope.open(schema)) {
			Bet bet = new Bet(UUID.randomUUID(), newBettingHouseId(), newSportId(), newLeagueId(), newMarketId(), null,
					UUID.randomUUID(), null, null, null, null, null, null, BigDecimal.TEN, BigDecimal.valueOf(2),
					BetStatus.PENDING, Instant.now(), "idem-key-2");

			betRepository.save(bet);

			assertThat(betRepository.findByIdempotencyKey("idem-key-2")).map(Bet::id).contains(bet.id());
			assertThat(betRepository.findByIdempotencyKey("missing")).isEmpty();
		}
	}

	@Test
	void betsShouldBeIsolatedBetweenTenants() {
		UUID betId = UUID.randomUUID();
		try (var _ = TenantContextScope.open(schema)) {
			betRepository.save(new Bet(betId, newBettingHouseId(), newSportId(), newLeagueId(), newMarketId(), null,
					UUID.randomUUID(), null, null, null, null, null, null, BigDecimal.TEN, BigDecimal.valueOf(2),
					BetStatus.PENDING, Instant.now(), null));
		}

		String otherSlug = "test-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
		provisionTenantSchema.ensureSchemaExists(otherSlug);
		var otherSchema = TenantSchemaName.fromSlug(otherSlug);
		try (var _ = TenantContextScope.open(otherSchema)) {
			assertThat(betRepository.findById(betId)).isEmpty();
		} finally {
			jdbcTemplate.execute("DROP SCHEMA IF EXISTS \"" + otherSchema.value() + "\" CASCADE");
		}
	}
}
