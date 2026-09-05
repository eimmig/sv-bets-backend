package com.stakevault.betting.bets.adapter.out.persistence;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "market")
@NoArgsConstructor
public class MarketJpaEntity extends CatalogJpaEntity {

	public MarketJpaEntity(UUID id, String name) {
		super(id, name);
	}
}
