package com.stakevault.betting.bets.application;

import org.springframework.stereotype.Service;

import com.stakevault.betting.bets.domain.model.TenantSchemaName;
import com.stakevault.betting.bets.domain.model.TenantSchemaNotFoundException;
import com.stakevault.betting.bets.domain.port.in.ProvisionTenantSchemaUseCase;
import com.stakevault.betting.bets.domain.port.out.TenantSchemaGateway;

@Service
public class ProvisionTenantSchemaService implements ProvisionTenantSchemaUseCase {

	private final TenantSchemaGateway gateway;

	public ProvisionTenantSchemaService(TenantSchemaGateway gateway) {
		this.gateway = gateway;
	}

	@Override
	public boolean exists(String tenantSlug) {
		return gateway.exists(TenantSchemaName.fromSlug(tenantSlug));
	}

	@Override
	public void ensureSchemaExists(String tenantSlug) {
		gateway.createAndMigrate(TenantSchemaName.fromSlug(tenantSlug));
	}

	@Override
	public void migrateIfPending(String tenantSlug) {
		TenantSchemaName schema = TenantSchemaName.fromSlug(tenantSlug);
		if (!gateway.exists(schema)) {
			throw new TenantSchemaNotFoundException(schema);
		}
		gateway.migrateExistingOnly(schema);
	}
}
