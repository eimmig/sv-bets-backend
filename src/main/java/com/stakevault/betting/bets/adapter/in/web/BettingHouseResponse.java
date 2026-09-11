package com.stakevault.betting.bets.adapter.in.web;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.stakevault.betting.bets.domain.model.BettingHouseBalance;

public record BettingHouseResponse(UUID id, String name, BigDecimal initialBalance, BigDecimal balance,
		Instant createdAt) {

	static BettingHouseResponse from(BettingHouseBalance bettingHouseBalance) {
		return new BettingHouseResponse(bettingHouseBalance.bettingHouse().id(), bettingHouseBalance.bettingHouse().name(),
				bettingHouseBalance.bettingHouse().initialBalance(), bettingHouseBalance.balance(),
				bettingHouseBalance.bettingHouse().createdAt());
	}
}
