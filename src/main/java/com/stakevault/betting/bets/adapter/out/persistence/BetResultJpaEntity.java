package com.stakevault.betting.bets.adapter.out.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.stakevault.betting.bets.domain.model.BetResult;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bet_result")
@Getter
@NoArgsConstructor
public class BetResultJpaEntity extends AbstractJpaEntity {

	@Column(name = "bet_id", nullable = false)
	private UUID betId;

	@Column(name = "settled_by_user_id", nullable = false)
	private UUID settledByUserId;

	@Column(nullable = false)
	private BigDecimal profit;

	@Column(name = "settled_at", nullable = false)
	private Instant settledAt;

	public BetResultJpaEntity(BetResult betResult) {
		super(betResult.id());
		this.betId = betResult.betId();
		this.settledByUserId = betResult.settledByUserId();
		this.profit = betResult.profit();
		this.settledAt = betResult.settledAt();
	}
}
