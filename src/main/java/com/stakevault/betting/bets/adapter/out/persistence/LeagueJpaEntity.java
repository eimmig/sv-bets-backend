package com.stakevault.betting.bets.adapter.out.persistence;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "league")
@NoArgsConstructor
public class LeagueJpaEntity extends CatalogJpaEntity {

	public LeagueJpaEntity(UUID id, String name) {
		super(id, name);
	}
}
