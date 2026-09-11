package com.stakevault.betting.bets.domain.port.in;

import com.stakevault.betting.bets.domain.model.TenantSchemaName;

public interface AdminProvisionTenantUseCase {

	TenantSchemaName provisionTenant(String slug);
}
