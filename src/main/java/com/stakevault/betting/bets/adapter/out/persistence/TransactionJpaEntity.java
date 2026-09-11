package com.stakevault.betting.bets.adapter.out.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.stakevault.betting.bets.domain.model.TransactionType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "transaction")
@Getter
@NoArgsConstructor
public class TransactionJpaEntity extends AbstractJpaEntity {

	@Column(name = "betting_house_id", nullable = false)
	private UUID bettingHouseId;

	@Column(nullable = false)
	private TransactionType type;

	@Column(nullable = false)
	private BigDecimal amount;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	public TransactionJpaEntity(UUID id, UUID bettingHouseId, TransactionType type, BigDecimal amount, Instant createdAt) {
		super(id);
		this.bettingHouseId = bettingHouseId;
		this.type = type;
		this.amount = amount;
		this.createdAt = createdAt;
	}
}
