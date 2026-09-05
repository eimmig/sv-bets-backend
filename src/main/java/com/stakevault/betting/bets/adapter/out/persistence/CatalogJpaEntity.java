package com.stakevault.betting.bets.adapter.out.persistence;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
public abstract class CatalogJpaEntity extends AbstractJpaEntity {

	@Column(nullable = false)
	private String name;

	protected CatalogJpaEntity(UUID id, String name) {
		super(id);
		this.name = name;
	}
}
