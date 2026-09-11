package com.stakevault.betting.bets.adapter.out.persistence;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tipster")
@NoArgsConstructor
public class TipsterJpaEntity extends CatalogJpaEntity {

	public TipsterJpaEntity(UUID id, String name) {
		super(id, name);
	}
}
