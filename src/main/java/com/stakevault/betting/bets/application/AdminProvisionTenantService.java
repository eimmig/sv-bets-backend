package com.stakevault.betting.bets.application;

import org.springframework.stereotype.Service;

import com.stakevault.betting.bets.domain.model.InvalidTenantSlugException;
import com.stakevault.betting.bets.domain.model.TenantAlreadyProvisionedException;
import com.stakevault.betting.bets.domain.model.TenantSchemaName;
import com.stakevault.betting.bets.domain.port.in.AdminProvisionTenantUseCase;
import com.stakevault.betting.bets.domain.port.in.ProvisionTenantSchemaUseCase;

@Service
public class AdminProvisionTenantService implements AdminProvisionTenantUseCase {

	private final ProvisionTenantSchemaUseCase provisionTenantSchema;

	public AdminProvisionTenantService(ProvisionTenantSchemaUseCase provisionTenantSchema) {
		this.provisionTenantSchema = provisionTenantSchema;
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
}
