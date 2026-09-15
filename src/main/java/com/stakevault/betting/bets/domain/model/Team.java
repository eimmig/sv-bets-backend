package com.stakevault.betting.bets.domain.model;

import java.util.UUID;

public record Team(UUID id, String name, UUID sportId) {

	public Team {
		if (id == null || name == null || name.isBlank() || sportId == null) {
			throw new IllegalArgumentException("dados de team invalidos");
		}
	}
}
