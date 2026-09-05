package com.stakevault.betting.bets.adapter.out.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "betting_house")
@Getter
@NoArgsConstructor
public class BettingHouseJpaEntity extends AbstractJpaEntity {

	@Column(nullable = false)
	private String name;

	@Column(name = "initial_balance", nullable = false)
	private BigDecimal initialBalance;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	public BettingHouseJpaEntity(UUID id, String name, BigDecimal initialBalance, Instant createdAt) {
		super(id);
		this.name = name;
		this.initialBalance = initialBalance;
		this.createdAt = createdAt;
	}
}
