package com.stakevault.betting.bets.application;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.stakevault.betting.bets.domain.model.TenantSettings;
import com.stakevault.betting.bets.domain.port.in.TenantSettingsUseCase;
import com.stakevault.betting.bets.domain.port.out.TenantSettingsRepository;

@Service
public class TenantSettingsService implements TenantSettingsUseCase {

	private final TenantSettingsRepository tenantSettingsRepository;

	public TenantSettingsService(TenantSettingsRepository tenantSettingsRepository) {
		this.tenantSettingsRepository = tenantSettingsRepository;
	}

	@Override
	public TenantSettings get() {
		return tenantSettingsRepository.find();
	}

	@Override
	public TenantSettings updateUnitPercent(BigDecimal unitPercent) {
		return tenantSettingsRepository.updateUnitPercent(unitPercent);
	}
}
