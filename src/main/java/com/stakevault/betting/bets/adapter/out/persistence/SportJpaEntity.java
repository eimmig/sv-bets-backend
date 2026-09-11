package com.stakevault.betting.bets.adapter.out.persistence;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sport")
@NoArgsConstructor
public class SportJpaEntity extends CatalogJpaEntity {

	public SportJpaEntity(UUID id, String name) {
		super(id, name);
	}
}
