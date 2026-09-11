package com.stakevault.betting.bets.domain.model;

import java.util.UUID;

public record League(UUID id, String name) {

	public League {
		if (id == null || name == null || name.isBlank()) {
			throw new IllegalArgumentException("dados de league invalidos");
		}
	}
}
