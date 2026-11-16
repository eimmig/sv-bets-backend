package com.stakevault.betting.bets.application;

import org.springframework.stereotype.Service;

import com.stakevault.betting.bets.domain.model.InvalidTenantSlugException;
import com.stakevault.betting.bets.domain.model.TenantAlreadyProvisionedException;
import com.stakevault.betting.bets.domain.model.TenantSchemaName;
import com.stakevault.betting.bets.domain.port.in.AdminProvisionTenantUseCase;
import com.stakevault.betting.bets.domain.port.in.ProvisionTenantSchemaUseCase;
import com.stakevault.betting.bets.domain.port.out.TenantSchemaGateway;

@Service
public class AdminProvisionTenantService implements AdminProvisionTenantUseCase {

	private final ProvisionTenantSchemaUseCase provisionTenantSchema;
	private final TenantSchemaGateway gateway;

	public AdminProvisionTenantService(ProvisionTenantSchemaUseCase provisionTenantSchema, TenantSchemaGateway gateway) {
		this.provisionTenantSchema = provisionTenantSchema;
		this.gateway = gateway;
	}

	@Override
	public TenantSchemaName provisionTenant(String slug) {
		TenantSchemaName schema;
		try {
			schema = TenantSchemaName.fromSlug(slug);
		} catch (IllegalArgumentException cause) {
			throw new InvalidTenantSlugException(slug, cause);
		}

		if (provisionTenantSchema.exists(slug)) {
			throw new TenantAlreadyProvisionedException(slug);
		}
		provisionTenantSchema.ensureSchemaExists(slug);
		return schema;
	}

	@Override
	public void deleteTenant(String slug) {
		TenantSchemaName schema;
		try {
			schema = TenantSchemaName.fromSlug(slug);
		} catch (IllegalArgumentException cause) {
			throw new InvalidTenantSlugException(slug, cause);
		}
		gateway.drop(schema, slug);
	}
}
