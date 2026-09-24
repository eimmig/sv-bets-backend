package com.stakevault.betting.bets.domain.port.in;

import java.math.BigDecimal;

import com.stakevault.betting.bets.domain.model.TenantSettings;

public interface TenantSettingsUseCase {

	TenantSettings get();

	TenantSettings updateUnitPercent(BigDecimal unitPercent);
}
