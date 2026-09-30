package com.stakevault.betting.bets.adapter.out.messaging;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "outbox_event", schema = "public")
@Getter
@NoArgsConstructor
public class OutboxEventJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String routingKey;

	@Column(nullable = false)
	private String payload;

	@Column(nullable = false)
	private String tenantSlug;

	@Column(nullable = false)
	private UUID betId;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	public OutboxEventJpaEntity(String routingKey, String payload, String tenantSlug, UUID betId, Instant createdAt) {
		this.routingKey = routingKey;
		this.payload = payload;
		this.tenantSlug = tenantSlug;
		this.betId = betId;
		this.createdAt = createdAt;
	}
}
