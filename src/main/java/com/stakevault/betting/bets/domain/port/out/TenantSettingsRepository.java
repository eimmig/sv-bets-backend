package com.stakevault.betting.bets.domain.port.out;

import java.math.BigDecimal;

import com.stakevault.betting.bets.domain.model.TenantSettings;

public interface TenantSettingsRepository {

	TenantSettings find();

	TenantSettings updateUnitPercent(BigDecimal unitPercent);
}
