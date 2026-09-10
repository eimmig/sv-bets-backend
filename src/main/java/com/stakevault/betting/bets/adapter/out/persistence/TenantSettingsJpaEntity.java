package com.stakevault.betting.bets.adapter.out.persistence;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tenant_settings")
@Getter
@NoArgsConstructor
public class TenantSettingsJpaEntity extends AbstractJpaEntity {

	@Column(name = "unit_percent", nullable = false)
	private BigDecimal unitPercent;

	public TenantSettingsJpaEntity(UUID id, BigDecimal unitPercent) {
		super(id);
		this.unitPercent = unitPercent;
	}

	void setUnitPercent(BigDecimal unitPercent) {
		this.unitPercent = unitPercent;
	}
}
