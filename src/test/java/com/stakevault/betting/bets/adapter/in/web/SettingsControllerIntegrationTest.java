package com.stakevault.betting.bets.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

import com.stakevault.betting.bets.domain.port.in.ProvisionTenantSchemaUseCase;
import com.stakevault.betting.bets.support.TenantSchemaIntegrationSupport;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SettingsControllerIntegrationTest extends TenantSchemaIntegrationSupport {

	@LocalServerPort
	private int port;

	private final HttpClient httpClient = HttpClient.newHttpClient();

	SettingsControllerIntegrationTest(ProvisionTenantSchemaUseCase provisionTenantSchema, JdbcTemplate jdbcTemplate) {
		super(provisionTenantSchema, jdbcTemplate);
	}

	private HttpResponse<String> get() throws Exception {
		HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/settings"))
				.header("X-Tenant-Id", tenantSlug)
				.GET()
				.build();
		return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
	}

	private HttpResponse<String> patch(String body, String role) throws Exception {
		HttpRequest.Builder builder = HttpRequest
				.newBuilder(URI.create("http://localhost:" + port + "/api/v1/settings"))
				.header("Content-Type", "application/json")
				.header("X-Tenant-Id", tenantSlug)
				.method("PATCH", HttpRequest.BodyPublishers.ofString(body));
		if (role != null) {
			builder.header("X-User-Role", role);
		}
		return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
	}

	@Test
	void shouldReturnSeededDefaultUnitPercent() throws Exception {
		HttpResponse<String> response = get();

		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.body()).contains("\"unitPercent\":0.01");
	}

	@Test
	void shouldUpdateUnitPercentWhenCallerIsAdmin() throws Exception {
		HttpResponse<String> patchResponse = patch("{\"unitPercent\":0.02}", "admin");

		assertThat(patchResponse.statusCode()).isEqualTo(200);
		assertThat(patchResponse.body()).contains("\"unitPercent\":0.02");

		HttpResponse<String> getResponse = get();
		assertThat(getResponse.body()).contains("\"unitPercent\":0.02");
	}

	@Test
	void shouldReturn403WhenCallerIsMember() throws Exception {
		HttpResponse<String> response = patch("{\"unitPercent\":0.02}", "member");

		assertThat(response.statusCode()).isEqualTo(403);
		assertThat(response.body()).contains("\"type\":\"https://docs/errors/admin-role-required\"");
	}

	@Test
	void shouldReturn403WhenRoleHeaderMissing() throws Exception {
		HttpResponse<String> response = patch("{\"unitPercent\":0.02}", null);

		assertThat(response.statusCode()).isEqualTo(403);
		assertThat(response.body()).contains("\"type\":\"https://docs/errors/admin-role-required\"");
	}

	@Test
	void shouldReturn400ForNegativeUnitPercent() throws Exception {
		HttpResponse<String> response = patch("{\"unitPercent\":-0.01}", "admin");

		assertThat(response.statusCode()).isEqualTo(400);
		assertThat(response.body()).contains("\"type\":\"https://docs/errors/validation-failed\"");
	}
}
