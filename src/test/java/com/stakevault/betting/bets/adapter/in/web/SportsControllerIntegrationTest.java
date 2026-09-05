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
class SportsControllerIntegrationTest extends TenantSchemaIntegrationSupport {

	@LocalServerPort
	private int port;

	private final HttpClient httpClient = HttpClient.newHttpClient();

	SportsControllerIntegrationTest(ProvisionTenantSchemaUseCase provisionTenantSchema, JdbcTemplate jdbcTemplate) {
		super(provisionTenantSchema, jdbcTemplate);
	}

	private HttpResponse<String> post(String body, String tenantSlugHeader) throws Exception {
		HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/sports"))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(body));
		if (tenantSlugHeader != null) {
			builder.header("X-Tenant-Id", tenantSlugHeader);
		}
		return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
	}

	private HttpResponse<String> get(String query, String tenantSlugHeader) throws Exception {
		HttpRequest.Builder builder = HttpRequest.newBuilder(
				URI.create("http://localhost:" + port + "/api/v1/sports" + query)).GET();
		if (tenantSlugHeader != null) {
			builder.header("X-Tenant-Id", tenantSlugHeader);
		}
		return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
	}

	@Test
	void shouldCreateAndListSport() throws Exception {
		HttpResponse<String> createResponse = post("{\"name\":\"Futebol\"}", tenantSlug);

		assertThat(createResponse.statusCode()).isEqualTo(201);
		assertThat(createResponse.body()).contains("\"name\":\"Futebol\"");

		HttpResponse<String> listResponse = get("?page=0&size=20", tenantSlug);

		assertThat(listResponse.statusCode()).isEqualTo(200);
		assertThat(listResponse.body()).contains("\"name\":\"Futebol\"");
		assertThat(listResponse.body()).contains("\"totalElements\":1");
	}

	@Test
	void shouldReturn409OnDuplicateNameWithinSameTenant() throws Exception {
		post("{\"name\":\"Basquete\"}", tenantSlug);

		HttpResponse<String> response = post("{\"name\":\"Basquete\"}", tenantSlug);

		assertThat(response.statusCode()).isEqualTo(409);
		assertThat(response.body()).contains("\"type\":\"https://docs/errors/sport-already-registered\"");
	}

	@Test
	void shouldReturn400ForBlankName() throws Exception {
		HttpResponse<String> response = post("{\"name\":\"\"}", tenantSlug);

		assertThat(response.statusCode()).isEqualTo(400);
		assertThat(response.body()).contains("\"type\":\"https://docs/errors/validation-failed\"");
	}

	@Test
	void shouldReturn400WhenTenantHeaderMissing() throws Exception {
		HttpResponse<String> response = post("{\"name\":\"Volei\"}", null);

		assertThat(response.statusCode()).isEqualTo(400);
		assertThat(response.body()).contains("\"type\":\"https://docs/errors/missing-tenant-id\"");
	}

	@Test
	void shouldClampNegativePageAndNonPositiveSizeInsteadOfFailing() throws Exception {
		post("{\"name\":\"Rugby\"}", tenantSlug);

		HttpResponse<String> response = get("?page=-5&size=0", tenantSlug);

		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.body()).contains("\"page\":0");
		assertThat(response.body()).contains("\"name\":\"Rugby\"");
	}

	@Test
	void shouldIsolateCatalogsBetweenTenantSchemas() throws Exception {
		String otherSlug = "test-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
		TenantSchemaName otherSchema = TenantSchemaName.fromSlug(otherSlug);
		provisionTenantSchema.ensureSchemaExists(otherSlug);

		try {
			post("{\"name\":\"Natacao\"}", tenantSlug);

			HttpResponse<String> otherTenantList = get("?page=0&size=20", otherSlug);

			assertThat(otherTenantList.statusCode()).isEqualTo(200);
			assertThat(otherTenantList.body()).doesNotContain("Natacao");
			assertThat(otherTenantList.body()).contains("\"totalElements\":0");
		} finally {
			jdbcTemplate.execute("DROP SCHEMA IF EXISTS \"" + otherSchema.value() + "\" CASCADE");
		}
	}
}
