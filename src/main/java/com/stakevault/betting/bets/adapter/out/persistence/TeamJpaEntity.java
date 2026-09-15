package com.stakevault.betting.bets.adapter.out.persistence;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "team")
@Getter
@NoArgsConstructor
public class TeamJpaEntity extends CatalogJpaEntity {

	@Column(name = "sport_id", nullable = false)
	private UUID sportId;

	public TeamJpaEntity(UUID id, String name, UUID sportId) {
		super(id, name);
		this.sportId = sportId;
	}
}
