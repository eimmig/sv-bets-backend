package com.stakevault.betting.bets.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

import com.stakevault.betting.bets.domain.port.in.ProvisionTenantSchemaUseCase;
import com.stakevault.betting.bets.support.TenantSchemaIntegrationSupport;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BetsControllerIntegrationTest extends TenantSchemaIntegrationSupport {

	@LocalServerPort
	private int port;

	private final HttpClient httpClient = HttpClient.newHttpClient();

	BetsControllerIntegrationTest(ProvisionTenantSchemaUseCase provisionTenantSchema, JdbcTemplate jdbcTemplate) {
		super(provisionTenantSchema, jdbcTemplate);
	}

	private String extractId(String body) {
		int start = body.indexOf("\"id\":\"") + 6;
		return body.substring(start, body.indexOf('"', start));
	}

	private String newCatalogId(String path, String name) throws Exception {
		HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
				.header("Content-Type", "application/json")
				.header("X-Tenant-Id", tenantSlug)
				.POST(HttpRequest.BodyPublishers.ofString("{\"name\":\"" + name + "\"}"))
				.build();
		return extractId(httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body());
	}

	private String newBettingHouseId() throws Exception {
		HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/betting-houses"))
				.header("Content-Type", "application/json")
				.header("X-Tenant-Id", tenantSlug)
				.POST(HttpRequest.BodyPublishers.ofString("{\"name\":\"House-" + UUID.randomUUID() + "\",\"initialBalance\":0}"))
				.build();
		return extractId(httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body());
	}

	private record References(String bettingHouseId, String sportId, String leagueId, String marketId) {
	}

	private References newReferences() throws Exception {
		return new References(newBettingHouseId(), newCatalogId("/api/v1/sports", "Sport-" + UUID.randomUUID()),
				newCatalogId("/api/v1/leagues", "League-" + UUID.randomUUID()),
				newCatalogId("/api/v1/markets", "Market-" + UUID.randomUUID()));
	}

	private String bodyFor(References refs, String tipsterId, String odd, String stake) {
		return "{\"bettingHouseId\":\"" + refs.bettingHouseId() + "\",\"sportId\":\"" + refs.sportId()
				+ "\",\"leagueId\":\"" + refs.leagueId() + "\",\"marketId\":\"" + refs.marketId() + "\","
				+ (tipsterId != null ? "\"tipsterId\":\"" + tipsterId + "\"," : "") + "\"stake\":" + stake + ",\"odd\":"
				+ odd + ",\"betDate\":\"2026-09-05T12:00:00Z\"}";
	}

	private HttpResponse<String> post(String body, String callerId, String idempotencyKey) throws Exception {
		HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/bets"))
				.header("Content-Type", "application/json")
				.header("X-Tenant-Id", tenantSlug)
				.POST(HttpRequest.BodyPublishers.ofString(body));
		if (callerId != null) {
			builder.header("X-User-Id", callerId);
		}
		if (idempotencyKey != null) {
			builder.header("Idempotency-Key", idempotencyKey);
		}
		return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
	}

	private HttpResponse<String> get(String id, String tenantSlugHeader) throws Exception {
		HttpRequest.Builder builder = HttpRequest
				.newBuilder(URI.create("http://localhost:" + port + "/api/v1/bets/" + id)).GET();
		if (tenantSlugHeader != null) {
			builder.header("X-Tenant-Id", tenantSlugHeader);
		}
		return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
	}

	private HttpResponse<String> patchStatus(String id, String status) throws Exception {
		HttpRequest request = HttpRequest
				.newBuilder(URI.create("http://localhost:" + port + "/api/v1/bets/" + id + "/status"))
				.header("Content-Type", "application/json")
				.header("X-Tenant-Id", tenantSlug)
				.method("PATCH", HttpRequest.BodyPublishers.ofString("{\"status\":\"" + status + "\"}"))
				.build();
		return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
	}

	private String createBetId() throws Exception {
		References refs = newReferences();
		HttpResponse<String> response = post(bodyFor(refs, null, "1.5", "100"), UUID.randomUUID().toString(), null);
		return extractId(response.body());
	}

	@Test
	void shouldCreateBetWithAllFields() throws Exception {
		References refs = newReferences();
		String tipsterId = newCatalogId("/api/v1/tipsters", "Tipster-" + UUID.randomUUID());

		HttpResponse<String> response = post(bodyFor(refs, tipsterId, "1.5", "100"), UUID.randomUUID().toString(), null);

		assertThat(response.statusCode()).isEqualTo(201);
		assertThat(response.body()).contains("\"status\":\"pending\"");
	}

	@Test
	void shouldCreateBetWithoutOptionalTipster() throws Exception {
		References refs = newReferences();

		HttpResponse<String> response = post(bodyFor(refs, null, "1.5", "100"), UUID.randomUUID().toString(), null);

		assertThat(response.statusCode()).isEqualTo(201);
	}

	@Test
	void shouldReturn401WhenCallerHeaderMissing() throws Exception {
		References refs = newReferences();

		HttpResponse<String> response = post(bodyFor(refs, null, "1.5", "100"), null, null);

		assertThat(response.statusCode()).isEqualTo(401);
		assertThat(response.body()).contains("\"type\":\"https://docs/errors/missing-caller-context\"");
	}

	@Test
	void shouldReturn404WhenBettingHouseDoesNotExist() throws Exception {
		References refs = newReferences();
		String body = "{\"bettingHouseId\":\"" + UUID.randomUUID() + "\",\"sportId\":\"" + refs.sportId()
				+ "\",\"leagueId\":\"" + refs.leagueId() + "\",\"marketId\":\"" + refs.marketId()
				+ "\",\"stake\":100,\"odd\":1.5,\"betDate\":\"2026-09-05T12:00:00Z\"}";

		HttpResponse<String> response = post(body, UUID.randomUUID().toString(), null);

		assertThat(response.statusCode()).isEqualTo(404);
		assertThat(response.body()).contains("\"type\":\"https://docs/errors/betting-house-not-found\"");
	}

	@Test
	void shouldReturn404WhenTipsterDoesNotExist() throws Exception {
		References refs = newReferences();

		HttpResponse<String> response = post(bodyFor(refs, UUID.randomUUID().toString(), "1.5", "100"),
				UUID.randomUUID().toString(), null);

		assertThat(response.statusCode()).isEqualTo(404);
		assertThat(response.body()).contains("\"type\":\"https://docs/errors/tipster-not-found\"");
	}

	@Test
	void shouldReturn422ForInvalidOdd() throws Exception {
		References refs = newReferences();

		HttpResponse<String> response = post(bodyFor(refs, null, "0.95", "100"), UUID.randomUUID().toString(), null);

		assertThat(response.statusCode()).isEqualTo(422);
		assertThat(response.body()).contains("\"type\":\"https://docs/errors/invalid-odd\"");
	}

	@Test
	void shouldReturn422ForInvalidStake() throws Exception {
		References refs = newReferences();

		HttpResponse<String> response = post(bodyFor(refs, null, "1.5", "0"), UUID.randomUUID().toString(), null);

		assertThat(response.statusCode()).isEqualTo(422);
		assertThat(response.body()).contains("\"type\":\"https://docs/errors/invalid-stake\"");
	}

	@Test
	void shouldReplayIdempotencyKeyInsteadOfDuplicating() throws Exception {
		References refs = newReferences();
		String idempotencyKey = "idem-" + UUID.randomUUID();

		HttpResponse<String> first = post(bodyFor(refs, null, "1.5", "100"), UUID.randomUUID().toString(), idempotencyKey);
		HttpResponse<String> second = post(bodyFor(refs, null, "1.5", "200"), UUID.randomUUID().toString(),
				idempotencyKey);

		assertThat(first.statusCode()).isEqualTo(201);
		assertThat(second.statusCode()).isEqualTo(200);
		assertThat(extractId(first.body())).isEqualTo(extractId(second.body()));
	}

	@Test
	void shouldGetBetById() throws Exception {
		String betId = createBetId();

		HttpResponse<String> response = get(betId, tenantSlug);

		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.body()).contains("\"id\":\"" + betId + "\"");
	}

	@Test
	void shouldReturn404WhenGettingUnknownBet() throws Exception {
		HttpResponse<String> response = get(UUID.randomUUID().toString(), tenantSlug);

		assertThat(response.statusCode()).isEqualTo(404);
		assertThat(response.body()).contains("\"type\":\"https://docs/errors/bet-not-found\"");
	}

	@Test
	void shouldTransitionPendingToWon() throws Exception {
		String betId = createBetId();

		HttpResponse<String> response = patchStatus(betId, "won");

		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.body()).contains("\"status\":\"won\"");
	}

	@Test
	void shouldTransitionPendingToLostAndVoid() throws Exception {
		assertThat(patchStatus(createBetId(), "lost").statusCode()).isEqualTo(200);
		assertThat(patchStatus(createBetId(), "void").statusCode()).isEqualTo(200);
	}

	@Test
	void shouldRejectTransitionFromAlreadySettledBet() throws Exception {
		String betId = createBetId();
		patchStatus(betId, "won");

		HttpResponse<String> response = patchStatus(betId, "lost");

		assertThat(response.statusCode()).isEqualTo(422);
		assertThat(response.body()).contains("\"type\":\"https://docs/errors/invalid-status-transition\"");
	}

	@Test
	void shouldRejectTransitionBackToPending() throws Exception {
		String betId = createBetId();

		HttpResponse<String> response = patchStatus(betId, "pending");

		assertThat(response.statusCode()).isEqualTo(422);
		assertThat(response.body()).contains("\"type\":\"https://docs/errors/invalid-status-transition\"");
	}
}
