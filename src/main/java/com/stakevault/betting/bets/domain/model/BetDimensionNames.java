package com.stakevault.betting.bets.domain.model;

// Nomes denormalizados no payload de BetCreated/BetSettled para stats-service popular
// DIM_BETTING_HOUSE/DIM_SPORT/DIM_LEAGUE/DIM_MARKET/DIM_TIPSTER sem chamada sincrona de volta a
// este servico - ver docs/API-CONTRACTS.md "Nomes das dimensoes denormalizados no payload".
public record BetDimensionNames(String bettingHouseName, String sportName, String leagueName, String marketName,
		String tipsterName) {
}
