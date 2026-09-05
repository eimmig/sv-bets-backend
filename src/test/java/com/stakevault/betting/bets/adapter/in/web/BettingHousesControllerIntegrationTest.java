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
class BettingHousesControllerIntegrationTest extends TenantSchemaIntegrationSupport {

	@LocalServerPort
	private int port;

	private final HttpClient httpClient = HttpClient.newHttpClient();

	BettingHousesControllerIntegrationTest(ProvisionTenantSchemaUseCase provisionTenantSchema, JdbcTemplate jdbcTemplate) {
		super(provisionTenantSchema, jdbcTemplate);
	}

	private HttpResponse<String> post(String body, String tenantSlugHeader) throws Exception {
		HttpRequest.Builder builder = HttpRequest
				.newBuilder(URI.create("http://localhost:" + port + "/api/v1/betting-houses"))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(body));
		if (tenantSlugHeader != null) {
			builder.header("X-Tenant-Id", tenantSlugHeader);
		}
		return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
	}

	private HttpResponse<String> get(String query, String tenantSlugHeader) throws Exception {
		HttpRequest.Builder builder = HttpRequest
				.newBuilder(URI.create("http://localhost:" + port + "/api/v1/betting-houses" + query)).GET();
		if (tenantSlugHeader != null) {
			builder.header("X-Tenant-Id", tenantSlugHeader);
		}
		return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
	}

	@Test
	void shouldCreateAndListWithBalanceEqualToInitialBalance() throws Exception {
		HttpResponse<String> createResponse = post("{\"name\":\"Bet365\",\"initialBalance\":100}", tenantSlug);

		assertThat(createResponse.statusCode()).isEqualTo(201);
		assertThat(createResponse.body()).contains("\"name\":\"Bet365\"").contains("\"balance\":100");

		HttpResponse<String> listResponse = get("?page=0&size=20", tenantSlug);

		assertThat(listResponse.statusCode()).isEqualTo(200);
		assertThat(listResponse.body()).contains("\"name\":\"Bet365\"").contains("\"totalElements\":1");
	}

	@Test
	void shouldReturn409OnDuplicateNameWithinSameTenant() throws Exception {
		post("{\"name\":\"Betano\",\"initialBalance\":0}", tenantSlug);

		HttpResponse<String> response = post("{\"name\":\"Betano\",\"initialBalance\":0}", tenantSlug);

		assertThat(response.statusCode()).isEqualTo(409);
		assertThat(response.body()).contains("\"type\":\"https://docs/errors/betting-house-already-registered\"");
	}

	@Test
	void shouldReturn400ForBlankName() throws Exception {
		HttpResponse<String> response = post("{\"name\":\"\",\"initialBalance\":0}", tenantSlug);

		assertThat(response.statusCode()).isEqualTo(400);
		assertThat(response.body()).contains("\"type\":\"https://docs/errors/validation-failed\"");
	}

	@Test
	void shouldReturn400ForNegativeInitialBalance() throws Exception {
		HttpResponse<String> response = post("{\"name\":\"KTO\",\"initialBalance\":-1}", tenantSlug);

		assertThat(response.statusCode()).isEqualTo(400);
		assertThat(response.body()).contains("\"type\":\"https://docs/errors/validation-failed\"");
	}

	@Test
	void shouldReturn400WhenTenantHeaderMissing() throws Exception {
		HttpResponse<String> response = post("{\"name\":\"Betfair\",\"initialBalance\":0}", null);

		assertThat(response.statusCode()).isEqualTo(400);
		assertThat(response.body()).contains("\"type\":\"https://docs/errors/missing-tenant-id\"");
	}

	@Test
	void shouldClampNegativePageAndNonPositiveSizeInsteadOfFailing() throws Exception {
		post("{\"name\":\"Rivalo\",\"initialBalance\":0}", tenantSlug);

		HttpResponse<String> response = get("?page=-5&size=0", tenantSlug);

		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.body()).contains("\"page\":0");
	}

	@Test
	void shouldIsolateBettingHousesBetweenTenantSchemas() throws Exception {
		String otherSlug = "test-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
		TenantSchemaName otherSchema = TenantSchemaName.fromSlug(otherSlug);
		provisionTenantSchema.ensureSchemaExists(otherSlug);

		try {
			post("{\"name\":\"Sportingbet\",\"initialBalance\":0}", tenantSlug);

			HttpResponse<String> otherTenantList = get("?page=0&size=20", otherSlug);

			assertThat(otherTenantList.statusCode()).isEqualTo(200);
			assertThat(otherTenantList.body()).doesNotContain("Sportingbet");
			assertThat(otherTenantList.body()).contains("\"totalElements\":0");
		} finally {
			jdbcTemplate.execute("DROP SCHEMA IF EXISTS \"" + otherSchema.value() + "\" CASCADE");
		}
	}

	@Test
	void shouldReflectDepositsAndWithdrawalsInComputedBalance() throws Exception {
		HttpResponse<String> createResponse = post("{\"name\":\"Pinnacle\",\"initialBalance\":100}", tenantSlug);
		String bettingHouseId = extractId(createResponse.body());

		postTransaction(bettingHouseId, "deposit", "50");
		postTransaction(bettingHouseId, "withdrawal", "30");

		HttpResponse<String> listResponse = get("?page=0&size=20", tenantSlug);

		assertThat(listResponse.statusCode()).isEqualTo(200);
		assertThat(listResponse.body()).contains("\"balance\":120");
	}

	private void postTransaction(String bettingHouseId, String type, String amount) throws Exception {
		HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/transactions"))
				.header("Content-Type", "application/json")
				.header("X-Tenant-Id", tenantSlug)
				.POST(HttpRequest.BodyPublishers
						.ofString("{\"bettingHouseId\":\"" + bettingHouseId + "\",\"type\":\"" + type + "\",\"amount\":" + amount + "}"))
				.build();
		httpClient.send(request, HttpResponse.BodyHandlers.ofString());
	}

	private static String extractId(String responseBody) {
		return responseBody.replaceFirst(".*\"id\":\"([0-9a-f-]+)\".*", "$1");
	}
}
