package com.stakevault.betting.bets.domain.port.in;

import java.math.BigDecimal;

import com.stakevault.betting.bets.domain.model.TenantSettings;

public interface TenantSettingsUseCase {

	TenantSettings get();

	// Caller must already be confirmed admin (adapter/in/web checks X-User-Role) - this layer
	// only applies the change, same split as BetUseCase.updateStatus trusting its caller param.
	TenantSettings updateUnitPercent(BigDecimal unitPercent);
}
