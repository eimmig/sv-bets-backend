package com.stakevault.betting.bets.domain.model;

import java.util.UUID;

public record Tipster(UUID id, String name) {

	public Tipster {
		if (id == null || name == null || name.isBlank()) {
			throw new IllegalArgumentException("dados de tipster invalidos");
		}
	}
}
