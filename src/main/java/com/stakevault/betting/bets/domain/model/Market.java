package com.stakevault.betting.bets.domain.model;

import java.util.UUID;

public record Market(UUID id, String name) {

	public Market {
		if (id == null || name == null || name.isBlank()) {
			throw new IllegalArgumentException("dados de market invalidos");
		}
	}
}
