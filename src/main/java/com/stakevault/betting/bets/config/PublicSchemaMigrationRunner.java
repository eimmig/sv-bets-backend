package com.stakevault.betting.bets.config;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
public class PublicSchemaMigrationRunner implements InitializingBean {

	private static final String MIGRATION_LOCATION = "classpath:db/migration-public";
	private static final String SCHEMA = "public";

	private final DataSource dataSource;

	public PublicSchemaMigrationRunner(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public void afterPropertiesSet() {
		Flyway.configure()
				.dataSource(dataSource)
				.schemas(SCHEMA)
				.createSchemas(false)
				.locations(MIGRATION_LOCATION)
				.load()
				.migrate();
	}
}
