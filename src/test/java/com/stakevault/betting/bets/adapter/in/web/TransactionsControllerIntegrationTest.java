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

import com.stakevault.betting.bets.domain.model.TenantSchemaName;
import com.stakevault.betting.bets.domain.port.in.ProvisionTenantSchemaUseCase;
import com.stakevault.betting.bets.support.TenantSchemaIntegrationSupport;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TransactionsControllerIntegrationTest extends TenantSchemaIntegrationSupport {

	@LocalServerPort
	private int port;

	private final HttpClient httpClient = HttpClient.newHttpClient();

	TransactionsControllerIntegrationTest(ProvisionTenantSchemaUseCase provisionTenantSchema, JdbcTemplate jdbcTemplate) {
		super(provisionTenantSchema, jdbcTemplate);
	}

	private HttpResponse<String> postBettingHouse(String name, String tenantSlugHeader) throws Exception {
		HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/betting-houses"))
				.header("Content-Type", "application/json")
				.header("X-Tenant-Id", tenantSlugHeader)
				.POST(HttpRequest.BodyPublishers.ofString("{\"name\":\"" + name + "\",\"initialBalance\":0}"))
				.build();
		return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
	}

	private String newBettingHouseId(String tenantSlugHeader) throws Exception {
		HttpResponse<String> response = postBettingHouse("House-" + UUID.randomUUID(), tenantSlugHeader);
		String body = response.body();
		int start = body.indexOf("\"id\":\"") + 6;
		return body.substring(start, body.indexOf('"', start));
	}

	private HttpResponse<String> post(String body, String tenantSlugHeader) throws Exception {
		HttpRequest.Builder builder = HttpRequest
				.newBuilder(URI.create("http://localhost:" + port + "/api/v1/transactions"))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(body));
		if (tenantSlugHeader != null) {
			builder.header("X-Tenant-Id", tenantSlugHeader);
		}
		return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
	}

	private HttpResponse<String> get(String query, String tenantSlugHeader) throws Exception {
		HttpRequest.Builder builder = HttpRequest
				.newBuilder(URI.create("http://localhost:" + port + "/api/v1/transactions" + query)).GET();
		if (tenantSlugHeader != null) {
			builder.header("X-Tenant-Id", tenantSlugHeader);
		}
		return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
	}

	@Test
	void shouldCreateDepositTransaction() throws Exception {
		String bettingHouseId = newBettingHouseId(tenantSlug);

		HttpResponse<String> response = post(
				"{\"bettingHouseId\":\"" + bettingHouseId + "\",\"type\":\"deposit\",\"amount\":50}", tenantSlug);

		assertThat(response.statusCode()).isEqualTo(201);
		assertThat(response.body()).contains("\"type\":\"deposit\"").contains("\"amount\":50");
	}

	@Test
	void shouldReturn404WhenBettingHouseDoesNotExist() throws Exception {
		HttpResponse<String> response = post(
				"{\"bettingHouseId\":\"" + UUID.randomUUID() + "\",\"type\":\"deposit\",\"amount\":50}", tenantSlug);

		assertThat(response.statusCode()).isEqualTo(404);
		assertThat(response.body()).contains("\"type\":\"https://docs/errors/betting-house-not-found\"");
	}

	@Test
	void shouldReturn400ForNonPositiveAmount() throws Exception {
		String bettingHouseId = newBettingHouseId(tenantSlug);

		HttpResponse<String> response = post(
				"{\"bettingHouseId\":\"" + bettingHouseId + "\",\"type\":\"deposit\",\"amount\":0}", tenantSlug);

		assertThat(response.statusCode()).isEqualTo(400);
		assertThat(response.body()).contains("\"type\":\"https://docs/errors/validation-failed\"");
	}

	@Test
	void shouldReturn400ForUnknownTransactionType() throws Exception {
		String bettingHouseId = newBettingHouseId(tenantSlug);

		HttpResponse<String> response = post(
				"{\"bettingHouseId\":\"" + bettingHouseId + "\",\"type\":\"bogus\",\"amount\":10}", tenantSlug);

		assertThat(response.statusCode()).isEqualTo(400);
		assertThat(response.body()).contains("\"type\":\"https://docs/errors/validation-failed\"");
	}

	@Test
	void shouldReturn400WhenTenantHeaderMissing() throws Exception {
		HttpResponse<String> response = post(
				"{\"bettingHouseId\":\"" + UUID.randomUUID() + "\",\"type\":\"deposit\",\"amount\":10}", null);

		assertThat(response.statusCode()).isEqualTo(400);
		assertThat(response.body()).contains("\"type\":\"https://docs/errors/missing-tenant-id\"");
	}

	@Test
	void shouldFilterByBettingHouseId() throws Exception {
		String houseA = newBettingHouseId(tenantSlug);
		String houseB = newBettingHouseId(tenantSlug);
		post("{\"bettingHouseId\":\"" + houseA + "\",\"type\":\"deposit\",\"amount\":10}", tenantSlug);
		post("{\"bettingHouseId\":\"" + houseB + "\",\"type\":\"deposit\",\"amount\":20}", tenantSlug);

		HttpResponse<String> response = get("?bettingHouseId=" + houseA + "&page=0&size=20", tenantSlug);

		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.body()).contains("\"totalElements\":1").contains("\"amount\":10");
	}

	@Test
	void shouldClampNegativePageAndNonPositiveSizeInsteadOfFailing() throws Exception {
		HttpResponse<String> response = get("?page=-1&size=0", tenantSlug);

		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.body()).contains("\"page\":0");
	}

	@Test
	void shouldIsolateTransactionsBetweenTenantSchemas() throws Exception {
		String otherSlug = "test-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
		TenantSchemaName otherSchema = TenantSchemaName.fromSlug(otherSlug);
		provisionTenantSchema.ensureSchemaExists(otherSlug);

		try {
			String bettingHouseId = newBettingHouseId(tenantSlug);
			post("{\"bettingHouseId\":\"" + bettingHouseId + "\",\"type\":\"deposit\",\"amount\":10}", tenantSlug);

			HttpResponse<String> otherTenantList = get("?page=0&size=20", otherSlug);

			assertThat(otherTenantList.statusCode()).isEqualTo(200);
			assertThat(otherTenantList.body()).contains("\"totalElements\":0");
		} finally {
			jdbcTemplate.execute("DROP SCHEMA IF EXISTS \"" + otherSchema.value() + "\" CASCADE");
		}
	}
}
