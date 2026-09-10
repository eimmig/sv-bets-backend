package com.stakevault.betting.bets.domain.model;

import java.math.BigDecimal;

// Single row per tenant schema (seeded by migration, never created via the application) -
// no id exposed at the domain level, see TenantSettingsRepository.
public record TenantSettings(BigDecimal unitPercent) {

	public TenantSettings {
		if (unitPercent == null || unitPercent.signum() < 0) {
			throw new IllegalArgumentException("dados de tenant settings invalidos");
		}
	}
}
