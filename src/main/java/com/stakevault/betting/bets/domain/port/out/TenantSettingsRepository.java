package com.stakevault.betting.bets.domain.port.out;

import java.math.BigDecimal;

import com.stakevault.betting.bets.domain.model.TenantSettings;

public interface TenantSettingsRepository {

	// Single row per tenant schema, seeded by migration - never absent for a provisioned tenant.
	TenantSettings find();

	TenantSettings updateUnitPercent(BigDecimal unitPercent);
}
