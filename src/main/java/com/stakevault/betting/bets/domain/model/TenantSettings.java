package com.stakevault.betting.bets.domain.model;

import java.math.BigDecimal;

public record TenantSettings(BigDecimal unitPercent) {

	public TenantSettings {
		if (unitPercent == null || unitPercent.signum() < 0) {
			throw new IllegalArgumentException("dados de tenant settings invalidos");
		}
	}
}
