package com.stakevault.betting.bets.domain.port.out;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import com.stakevault.betting.bets.domain.model.BettingHouse;
import com.stakevault.betting.bets.domain.model.PagedResult;

public interface BettingHouseRepository {

	BettingHouse save(BettingHouse bettingHouse);

	boolean existsByName(String name);

	boolean existsById(UUID id);

	Optional<BettingHouse> findById(UUID id);

	PagedResult<BettingHouse> findAll(int page, int size);

	// Todas as casas do tenant, nao agrupado - usado por GET /api/v1/bankroll/balance (feat-014.3).
	BigDecimal sumInitialBalance();
}
