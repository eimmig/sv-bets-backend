package com.stakevault.betting.bets.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

import com.stakevault.betting.bets.domain.port.in.ProvisionTenantSchemaUseCase;
import com.stakevault.betting.bets.support.TenantSchemaIntegrationSupport;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TeamsControllerIntegrationTest extends TenantSchemaIntegrationSupport {

	private static final Pattern ID_PATTERN = Pattern.compile("\"id\":\"([0-9a-fA-F-]{36})\"");

	@LocalServerPort
	private int port;

	private final HttpClient httpClient = HttpClient.newHttpClient();

	TeamsControllerIntegrationTest(ProvisionTenantSchemaUseCase provisionTenantSchema, JdbcTemplate jdbcTemplate) {
		super(provisionTenantSchema, jdbcTemplate);
	}

	private HttpResponse<String> post(String path, String body) throws Exception {
		HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
				.header("Content-Type", "application/json")
				.header("X-Tenant-Id", tenantSlug)
				.POST(HttpRequest.BodyPublishers.ofString(body))
				.build();
		return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
	}

	private HttpResponse<String> get(String path) throws Exception {
		HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
				.header("X-Tenant-Id", tenantSlug)
				.GET()
				.build();
		return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
	}

	private String createSport(String name) throws Exception {
		HttpResponse<String> response = post("/api/v1/sports", "{\"name\":\"" + name + "\"}");
		Matcher matcher = ID_PATTERN.matcher(response.body());
		if (!matcher.find()) {
			throw new IllegalStateException("sport id not found in response: " + response.body());
		}
		return matcher.group(1);
	}

	@Test
	void shouldCreateTeam() throws Exception {
		String sportId = createSport("CS");

		HttpResponse<String> response = post("/api/v1/teams",
				"{\"name\":\"Furia\",\"sportId\":\"" + sportId + "\"}");

		assertThat(response.statusCode()).isEqualTo(201);
		assertThat(response.body()).contains("\"name\":\"Furia\"").contains("\"sportId\":\"" + sportId + "\"");
	}

	@Test
	void shouldReturn404WhenSportDoesNotExist() throws Exception {
		HttpResponse<String> response = post("/api/v1/teams",
				"{\"name\":\"Furia\",\"sportId\":\"" + UUID.randomUUID() + "\"}");

		assertThat(response.statusCode()).isEqualTo(404);
		assertThat(response.body()).contains("\"type\":\"https://docs/errors/sport-not-found\"");
	}

	@Test
	void shouldReturn409OnDuplicateNameForSameSport() throws Exception {
		String sportId = createSport("LoL");
		post("/api/v1/teams", "{\"name\":\"Furia\",\"sportId\":\"" + sportId + "\"}");

		HttpResponse<String> response = post("/api/v1/teams",
				"{\"name\":\"Furia\",\"sportId\":\"" + sportId + "\"}");

		assertThat(response.statusCode()).isEqualTo(409);
		assertThat(response.body()).contains("\"type\":\"https://docs/errors/team-already-registered\"");
	}

	@Test
	void shouldAllowSameNameForDifferentSport() throws Exception {
		String cs = createSport("CS2");
		String lol = createSport("LoL2");
		post("/api/v1/teams", "{\"name\":\"Furia\",\"sportId\":\"" + cs + "\"}");

		HttpResponse<String> response = post("/api/v1/teams", "{\"name\":\"Furia\",\"sportId\":\"" + lol + "\"}");

		assertThat(response.statusCode()).isEqualTo(201);
	}

	@Test
	void shouldListTeams() throws Exception {
		String sportId = createSport("Basquete");
		post("/api/v1/teams", "{\"name\":\"Flamengo\",\"sportId\":\"" + sportId + "\"}");

		HttpResponse<String> response = get("/api/v1/teams");

		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.body()).contains("\"name\":\"Flamengo\"");
	}
}
