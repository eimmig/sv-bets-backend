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
class AdminTenantControllerIntegrationTest extends TenantSchemaIntegrationSupport {

	@LocalServerPort
	private int port;

	private final HttpClient httpClient = HttpClient.newHttpClient();

	AdminTenantControllerIntegrationTest(ProvisionTenantSchemaUseCase provisionTenantSchema, JdbcTemplate jdbcTemplate) {
		super(provisionTenantSchema, jdbcTemplate);
	}

	private HttpResponse<String> post(String body, String... headers) throws Exception {
		HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/admin/tenants"))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(body));
		for (int i = 0; i < headers.length; i += 2) {
			builder.header(headers[i], headers[i + 1]);
		}
		return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
	}

	@Test
	void shouldCreateTenantSchemaOnValidRequest() throws Exception {
		String newSlug = "test-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);

		HttpResponse<String> response = post(
				"{\"slug\":\"" + newSlug + "\"}",
				"X-Admin-Api-Key", "test-admin-api-key");

		try {
			assertThat(response.statusCode()).isEqualTo(201);
			assertThat(response.body()).contains("\"schema\":\"tenant_" + newSlug + "\"");
		} finally {
			jdbcTemplate.execute("DROP SCHEMA IF EXISTS \"tenant_" + newSlug + "\" CASCADE");
		}
	}

	@Test
	void shouldReturn409WhenSlugAlreadyProvisioned() throws Exception {
		HttpResponse<String> response = post(
				"{\"slug\":\"" + tenantSlug + "\"}",
				"X-Admin-Api-Key", "test-admin-api-key");

		assertThat(response.statusCode()).isEqualTo(409);
		assertThat(response.body()).contains("\"type\":\"https://docs/errors/tenant-already-provisioned\"");
		assertThat(response.body()).contains("\"instance\":\"/api/v1/admin/tenants\"");
	}

	@Test
	void shouldReturn422ForInvalidSlug() throws Exception {
		HttpResponse<String> response = post(
				"{\"slug\":\"1invalid\"}",
				"X-Admin-Api-Key", "test-admin-api-key");

		assertThat(response.statusCode()).isEqualTo(422);
		assertThat(response.body()).contains("\"type\":\"https://docs/errors/invalid-tenant-slug\"");
	}

	@Test
	void shouldReturn422WithoutLeakingNullWhenSlugIsMissing() throws Exception {
		HttpResponse<String> response = post(
				"{}",
				"X-Admin-Api-Key", "test-admin-api-key");

		assertThat(response.statusCode()).isEqualTo(422);
		assertThat(response.body()).doesNotContain("null");
	}

	@Test
	void shouldReturn401WithoutCreatingSchemaWhenAdminApiKeyMissing() throws Exception {
		String newSlug = "test-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);

		HttpResponse<String> response = post("{\"slug\":\"" + newSlug + "\"}");

		assertThat(response.statusCode()).isEqualTo(401);
		Boolean schemaExists = jdbcTemplate.queryForObject(
				"SELECT EXISTS (SELECT 1 FROM information_schema.schemata WHERE schema_name = ?)",
				Boolean.class, "tenant_" + newSlug);
		assertThat(schemaExists).isFalse();
	}

	@Test
	void shouldReturn401WhenAdminApiKeyIsWrong() throws Exception {
		HttpResponse<String> response = post(
				"{\"slug\":\"" + tenantSlug + "\"}",
				"X-Admin-Api-Key", "not-the-real-key");

		assertThat(response.statusCode()).isEqualTo(401);
	}

	@Test
	void shouldLocalizeErrorTitleAndDetailPerAcceptLanguage() throws Exception {
		HttpResponse<String> response = post(
				"{\"slug\":\"" + tenantSlug + "\"}",
				"X-Admin-Api-Key", "test-admin-api-key",
				"Accept-Language", "es");

		assertThat(response.statusCode()).isEqualTo(409);
		assertThat(response.body()).contains("El tenant ya existe");
	}

	@Test
	void shouldLocalizeAdminApiKeyErrorPerAcceptLanguage() throws Exception {
		HttpResponse<String> response = post(
				"{\"slug\":\"" + tenantSlug + "\"}",
				"Accept-Language", "es");

		assertThat(response.statusCode()).isEqualTo(401);
		assertThat(response.body()).contains("Clave de administrador inválida");
		assertThat(response.headers().firstValue("Content-Type"))
				.hasValueSatisfying(contentType -> assertThat(contentType).containsIgnoringCase("charset=UTF-8"));
	}

	@Test
	void shouldReturn401ForPercentEncodedPathBypassAttempt() throws Exception {
		HttpRequest request = HttpRequest.newBuilder(
				URI.create("http://localhost:" + port + "/api/v1/adm%69n/tenants"))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString("{\"slug\":\"" + tenantSlug + "\"}"))
				.build();

		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

		assertThat(response.statusCode()).isEqualTo(401);
	}


	private HttpResponse<String> delete(String slug, String... headers) throws Exception {
		HttpRequest.Builder builder = HttpRequest
				.newBuilder(URI.create("http://localhost:" + port + "/api/v1/admin/tenants/" + slug)).DELETE();
		for (int i = 0; i < headers.length; i += 2) {
			builder.header(headers[i], headers[i + 1]);
		}
		return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
	}

	@Test
	void shouldDropTheTenantSchemaAndItsPendingOutboxEvents() throws Exception {
		String slug = "del-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
		assertThat(post("{\"slug\":\"" + slug + "\"}", "X-Admin-Api-Key", "test-admin-api-key").statusCode())
				.isEqualTo(201);
		jdbcTemplate.update("INSERT INTO public.outbox_event (routing_key, payload, tenant_slug, bet_id, created_at)"
				+ " VALUES ('bet.created', '{}', ?, ?, now())", slug, UUID.randomUUID());

		HttpResponse<String> response = delete(slug, "X-Admin-Api-Key", "test-admin-api-key");

		assertThat(response.statusCode()).isEqualTo(204);
		assertThat(jdbcTemplate.queryForObject(
				"SELECT count(*) FROM information_schema.schemata WHERE schema_name = ?", Integer.class,
				"tenant_" + slug)).isZero();
		assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM public.outbox_event WHERE tenant_slug = ?",
				Integer.class, slug)).isZero();
	}

	@Test
	void shouldAnswer204WhenTheTenantDoesNotExist() throws Exception {
		assertThat(delete("nao-existe-" + UUID.randomUUID().toString().substring(0, 8), "X-Admin-Api-Key",
				"test-admin-api-key").statusCode()).isEqualTo(204);
	}

	@Test
	void shouldReturn422WhenDeletingAnInvalidSlug() throws Exception {
		HttpResponse<String> response = delete("1invalid", "X-Admin-Api-Key", "test-admin-api-key");

		assertThat(response.statusCode()).isEqualTo(422);
		assertThat(response.body()).contains("\"type\":\"https://docs/errors/invalid-tenant-slug\"");
	}

	@Test
	void shouldRejectDeletionWithoutTheAdminKey() throws Exception {
		assertThat(delete(tenantSlug).statusCode()).isEqualTo(401);
	}
}
