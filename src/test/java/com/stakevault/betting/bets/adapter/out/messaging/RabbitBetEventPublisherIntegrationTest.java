package com.stakevault.betting.bets.adapter.out.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.stakevault.betting.bets.TestcontainersConfiguration;
import com.stakevault.betting.bets.config.TenantContextScope;
import com.stakevault.betting.bets.domain.model.BetStatus;
import com.stakevault.betting.bets.domain.model.BetType;
import com.stakevault.betting.bets.domain.model.BettingHouse;
import com.stakevault.betting.bets.domain.model.InvalidStatusTransitionException;
import com.stakevault.betting.bets.domain.model.League;
import com.stakevault.betting.bets.domain.model.Market;
import com.stakevault.betting.bets.domain.model.Sport;
import com.stakevault.betting.bets.domain.port.in.BetUseCase;
import com.stakevault.betting.bets.domain.port.in.CreateBetCommand;
import com.stakevault.betting.bets.domain.port.in.ProvisionTenantSchemaUseCase;
import com.stakevault.betting.bets.domain.port.out.BettingHouseRepository;
import com.stakevault.betting.bets.domain.port.out.LeagueRepository;
import com.stakevault.betting.bets.domain.port.out.MarketRepository;
import com.stakevault.betting.bets.domain.port.out.SportRepository;
import com.stakevault.betting.bets.support.TenantSchemaIntegrationSupport;

class RabbitBetEventPublisherIntegrationTest extends TenantSchemaIntegrationSupport {

	private final BetUseCase bets;
	private final RabbitTemplate rabbitTemplate;
	private final BettingHouseRepository bettingHouseRepository;
	private final SportRepository sportRepository;
	private final LeagueRepository leagueRepository;
	private final MarketRepository marketRepository;

	RabbitBetEventPublisherIntegrationTest(ProvisionTenantSchemaUseCase provisionTenantSchema, JdbcTemplate jdbcTemplate,
			BetUseCase bets, RabbitTemplate rabbitTemplate, BettingHouseRepository bettingHouseRepository,
			SportRepository sportRepository, LeagueRepository leagueRepository, MarketRepository marketRepository) {
		super(provisionTenantSchema, jdbcTemplate);
		this.bets = bets;
		this.rabbitTemplate = rabbitTemplate;
		this.bettingHouseRepository = bettingHouseRepository;
		this.sportRepository = sportRepository;
		this.leagueRepository = leagueRepository;
		this.marketRepository = marketRepository;
	}

	private record BetFixture(CreateBetCommand command, String bettingHouseName, String sportName, String leagueName,
			String marketName) {
	}

	private BetFixture newCommand(UUID callerId, String idempotencyKey) {
		return newCommand(callerId, idempotencyKey, null);
	}

	private BetFixture newCommand(UUID callerId, String idempotencyKey, BetType betType) {
		String suffix = UUID.randomUUID().toString();
		String bettingHouseName = "House-" + suffix;
		String sportName = "Sport-" + suffix;
		String leagueName = "League-" + suffix;
		String marketName = "Market-" + suffix;
		UUID bettingHouseId = bettingHouseRepository
				.save(new BettingHouse(UUID.randomUUID(), bettingHouseName, BigDecimal.ZERO, Instant.now())).id();
		UUID sportId = sportRepository.save(new Sport(UUID.randomUUID(), sportName)).id();
		UUID leagueId = leagueRepository.save(new League(UUID.randomUUID(), leagueName)).id();
		UUID marketId = marketRepository.save(new Market(UUID.randomUUID(), marketName)).id();
		CreateBetCommand command = new CreateBetCommand(callerId, bettingHouseId, sportId, leagueId, marketId, null,
				null, null, null, null, betType, null, BigDecimal.valueOf(100), BigDecimal.valueOf(1.5),
				Instant.parse("2026-09-06T12:00:00Z"), idempotencyKey);
		return new BetFixture(command, bettingHouseName, sportName, leagueName, marketName);
	}

	private JsonNode validateAgainstSchema(byte[] body, String schemaResourcePath) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode node = objectMapper.readTree(body);
		JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
		try (InputStream schemaStream = getClass().getResourceAsStream(schemaResourcePath)) {
			JsonSchema schema = factory.getSchema(schemaStream);
			var errors = schema.validate(node);
			assertThat(errors).as("schema validation errors: %s", errors).isEmpty();
		}
		return node;
	}

	@Test
	void shouldPublishBetCreatedMatchingTheSchema() throws Exception {
		try (var _ = TenantContextScope.open(schema)) {
			UUID callerId = UUID.randomUUID();
			var fixture = newCommand(callerId, null);

			var result = bets.create(fixture.command());

			Message message = rabbitTemplate.receive(TestcontainersConfiguration.TEST_QUEUE, 5000);
			assertThat(message).isNotNull();
			JsonNode event = validateAgainstSchema(message.getBody(), "/contracts/bet-created.schema.json");

			assertThat(event.get("eventType").asText()).isEqualTo("BetCreated");
			assertThat(event.get("schemaVersion").asInt()).isEqualTo(1);
			assertThat(event.get("tenantId").asText()).isEqualTo(tenantSlug);
			assertThat(event.get("userId").asText()).isEqualTo(callerId.toString());
			JsonNode payload = event.get("payload");
			assertThat(payload.get("betId").asText()).isEqualTo(result.bet().id().toString());
			assertThat(payload.get("status").asText()).isEqualTo("pending");
			assertThat(payload.get("tipsterId").isNull()).isTrue();
			assertThat(payload.get("tipsterName").isNull()).isTrue();
			assertThat(payload.get("bettingHouseName").asText()).isEqualTo(fixture.bettingHouseName());
			assertThat(payload.get("sportName").asText()).isEqualTo(fixture.sportName());
			assertThat(payload.get("leagueName").asText()).isEqualTo(fixture.leagueName());
			assertThat(payload.get("marketName").asText()).isEqualTo(fixture.marketName());
			assertThat(message.getMessageProperties().getReceivedDeliveryMode())
					.isEqualTo(org.springframework.amqp.core.MessageDeliveryMode.PERSISTENT);
		}
	}

	@Test
	void shouldPublishBetTypeAsLowercaseEnumValueMatchingTheSchema() throws Exception {
		try (var _ = TenantContextScope.open(schema)) {
			var fixture = newCommand(UUID.randomUUID(), null, BetType.PRE);

			bets.create(fixture.command());

			Message message = rabbitTemplate.receive(TestcontainersConfiguration.TEST_QUEUE, 5000);
			assertThat(message).isNotNull();
			JsonNode event = validateAgainstSchema(message.getBody(), "/contracts/bet-created.schema.json");

			assertThat(event.get("payload").get("betType").asText()).isEqualTo("pre");
		}
	}

	@Test
	void shouldNotPublishAgainWhenIdempotencyKeyReplays() {
		try (var _ = TenantContextScope.open(schema)) {
			String idempotencyKey = "idem-" + UUID.randomUUID();
			var fixture = newCommand(UUID.randomUUID(), idempotencyKey);
			bets.create(fixture.command());
			assertThat(rabbitTemplate.receive(TestcontainersConfiguration.TEST_QUEUE, 5000)).isNotNull();

			bets.create(fixture.command());

			assertThat(rabbitTemplate.receive(TestcontainersConfiguration.TEST_QUEUE, 1000)).isNull();
		}
	}

	@Test
	void shouldPublishBetSettledMatchingTheSchema() throws Exception {
		try (var _ = TenantContextScope.open(schema)) {
			var fixture = newCommand(UUID.randomUUID(), null);
			var created = bets.create(fixture.command());
			assertThat(rabbitTemplate.receive(TestcontainersConfiguration.TEST_QUEUE, 5000)).isNotNull(); // BetCreated
			UUID settledByUserId = UUID.randomUUID();

			bets.updateStatus(created.bet().id(), BetStatus.WON, settledByUserId);

			Message message = rabbitTemplate.receive(TestcontainersConfiguration.TEST_QUEUE, 5000);
			assertThat(message).isNotNull();
			JsonNode event = validateAgainstSchema(message.getBody(), "/contracts/bet-settled.schema.json");

			assertThat(event.get("eventType").asText()).isEqualTo("BetSettled");
			assertThat(event.get("userId").asText()).isEqualTo(settledByUserId.toString());
			JsonNode payload = event.get("payload");
			assertThat(payload.get("betId").asText()).isEqualTo(created.bet().id().toString());
			assertThat(payload.get("status").asText()).isEqualTo("won");
			assertThat(payload.get("profit").asDouble()).isEqualTo(50.0);
			assertThat(payload.get("bettingHouseName").asText()).isEqualTo(fixture.bettingHouseName());
			assertThat(payload.get("sportName").asText()).isEqualTo(fixture.sportName());
			assertThat(payload.get("leagueName").asText()).isEqualTo(fixture.leagueName());
			assertThat(payload.get("marketName").asText()).isEqualTo(fixture.marketName());
			assertThat(message.getMessageProperties().getReceivedDeliveryMode())
					.isEqualTo(org.springframework.amqp.core.MessageDeliveryMode.PERSISTENT);
		}
	}

	@Test
	void shouldNotPublishBetSettledWhenTransitionIsInvalid() {
		try (var _ = TenantContextScope.open(schema)) {
			var created = bets.create(newCommand(UUID.randomUUID(), null).command());
			assertThat(rabbitTemplate.receive(TestcontainersConfiguration.TEST_QUEUE, 5000)).isNotNull(); // BetCreated
			bets.updateStatus(created.bet().id(), BetStatus.WON, UUID.randomUUID());
			assertThat(rabbitTemplate.receive(TestcontainersConfiguration.TEST_QUEUE, 5000)).isNotNull(); // BetSettled

			try {
				bets.updateStatus(created.bet().id(), BetStatus.LOST, UUID.randomUUID());
			} catch (InvalidStatusTransitionException _) {
				// already settled - expected
			}

			assertThat(rabbitTemplate.receive(TestcontainersConfiguration.TEST_QUEUE, 1000)).isNull();
		}
	}
}
