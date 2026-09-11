package com.stakevault.betting.bets.domain.model;

import java.util.UUID;

public record Sport(UUID id, String name) {

	public Sport {
		if (id == null || name == null || name.isBlank()) {
			throw new IllegalArgumentException("dados de sport invalidos");
		}
	}
}
