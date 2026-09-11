package com.stakevault.betting.bets.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import com.stakevault.betting.bets.domain.model.TenantSchemaName;
import com.stakevault.betting.bets.domain.model.TenantSchemaNotFoundException;
import com.stakevault.betting.bets.domain.port.in.ProvisionTenantSchemaUseCase;
import com.stakevault.betting.bets.support.TenantSchemaIntegrationSupport;

class JdbcFlywayTenantSchemaGatewayIntegrationTest extends TenantSchemaIntegrationSupport {

	JdbcFlywayTenantSchemaGatewayIntegrationTest(ProvisionTenantSchemaUseCase provisionTenantSchema, JdbcTemplate jdbcTemplate) {
		super(provisionTenantSchema, jdbcTemplate);
	}

	@Test
	void ensureSchemaExistsShouldCreateAndMigrateWithoutFailing() {
		Integer count = jdbcTemplate.queryForObject(
				"SELECT count(*) FROM information_schema.schemata WHERE schema_name = ?",
				Integer.class, schema.value());

		assertThat(count).isEqualTo(1);
	}

	@Test
	void ensureSchemaExistsShouldCreateCatalogTables() {
		for (String table : new String[] { "sport", "league", "market", "tipster", "betting_house", "transaction" }) {
			Integer count = jdbcTemplate.queryForObject(
					"SELECT count(*) FROM information_schema.tables WHERE table_schema = ? AND table_name = ?",
					Integer.class, schema.value(), table);
			assertThat(count).as("table %s in schema %s", table, schema.value()).isEqualTo(1);
		}
	}

	@Test
	void ensureSchemaExistsShouldBeIdempotent() {
		provisionTenantSchema.ensureSchemaExists(tenantSlug);

		Integer count = jdbcTemplate.queryForObject(
				"SELECT count(*) FROM information_schema.schemata WHERE schema_name = ?",
				Integer.class, schema.value());
		assertThat(count).isEqualTo(1);
	}

	@Test
	void migrateIfPendingShouldRunWithoutFailingWhenSchemaAlreadyExists() {
		assertThatCode(() -> provisionTenantSchema.migrateIfPending(tenantSlug)).doesNotThrowAnyException();
	}

	@Test
	void migrateIfPendingShouldThrowWithoutCreatingSchemaWhenTenantIsNotProvisioned() {
		String missingSlug = "test-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
		TenantSchemaName missingSchema = TenantSchemaName.fromSlug(missingSlug);

		assertThatThrownBy(() -> provisionTenantSchema.migrateIfPending(missingSlug))
				.isInstanceOf(TenantSchemaNotFoundException.class);

		Integer count = jdbcTemplate.queryForObject(
				"SELECT count(*) FROM information_schema.schemata WHERE schema_name = ?",
				Integer.class, missingSchema.value());
		assertThat(count).isZero();
	}
}
