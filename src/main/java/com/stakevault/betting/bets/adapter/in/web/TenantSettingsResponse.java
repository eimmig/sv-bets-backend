package com.stakevault.betting.bets.adapter.in.web;

import java.math.BigDecimal;

import com.stakevault.betting.bets.domain.model.TenantSettings;

public record TenantSettingsResponse(BigDecimal unitPercent) {

	public static TenantSettingsResponse from(TenantSettings settings) {
		return new TenantSettingsResponse(settings.unitPercent());
	}
}
