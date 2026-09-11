package com.stakevault.betting.bets.adapter.out.persistence;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.stakevault.betting.bets.domain.model.TenantSettings;
import com.stakevault.betting.bets.domain.port.out.TenantSettingsRepository;

@Repository
public class JpaTenantSettingsRepository implements TenantSettingsRepository {

	// Matches the literal id the seed migration inserts - single row per tenant schema, never
	// created by the application (see V*__create_tenant_settings_table.sql).
	static final UUID ROW_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

	private final TenantSettingsSpringDataRepository jpaRepository;

	public JpaTenantSettingsRepository(TenantSettingsSpringDataRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public TenantSettings find() {
		return toDomain(row());
	}

	@Override
	public TenantSettings updateUnitPercent(BigDecimal unitPercent) {
		TenantSettingsJpaEntity entity = row();
		entity.setUnitPercent(unitPercent);
		return toDomain(jpaRepository.save(entity));
	}

	private TenantSettingsJpaEntity row() {
		return jpaRepository.findById(ROW_ID)
				.orElseThrow(() -> new IllegalStateException("tenant_settings seed row missing - migration invariant broken"));
	}

	private static TenantSettings toDomain(TenantSettingsJpaEntity entity) {
		return new TenantSettings(entity.getUnitPercent());
	}
}
