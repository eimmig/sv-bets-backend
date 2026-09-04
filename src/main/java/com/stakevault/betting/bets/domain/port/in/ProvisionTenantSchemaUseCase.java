package com.stakevault.betting.bets.domain.port.in;

public interface ProvisionTenantSchemaUseCase {

	boolean exists(String tenantSlug);

	void ensureSchemaExists(String tenantSlug);

	void migrateIfPending(String tenantSlug);
}
