package com.stakevault.betting.bets.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.domain.Persistable;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
public abstract class CatalogJpaEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	@Column(nullable = false)
	private String name;

	@Transient
	private boolean isNew = true;

	protected CatalogJpaEntity(UUID id, String name) {
		this.id = id;
		this.name = name;
	}

	@Override
	public boolean isNew() {
		return isNew;
	}

	@PostLoad
	void markNotNew() {
		this.isNew = false;
	}
}
