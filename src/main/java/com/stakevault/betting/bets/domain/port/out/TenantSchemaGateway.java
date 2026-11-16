package com.stakevault.betting.bets.domain.port.out;

import com.stakevault.betting.bets.domain.model.TenantSchemaName;

public interface TenantSchemaGateway {

	boolean exists(TenantSchemaName schema);

	void createAndMigrate(TenantSchemaName schema);

	void migrateExistingOnly(TenantSchemaName schema);

	/**
	 * Apaga o schema da organizacao e os eventos dela ainda pendentes na outbox (epic-065). Nao falha
	 * quando o schema nao existe.
	 */
	void drop(TenantSchemaName schema, String tenantSlug);
}
