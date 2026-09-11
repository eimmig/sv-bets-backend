package com.stakevault.betting.bets.domain.model;

import java.math.BigDecimal;

public record BettingHouseBalance(BettingHouse bettingHouse, BigDecimal balance) {

	public BettingHouseBalance {
		if (bettingHouse == null || balance == null) {
			throw new IllegalArgumentException("dados de betting house balance invalidos");
		}
	}
}
