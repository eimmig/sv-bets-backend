package com.stakevault.betting.bets.domain.port.out;

import java.util.UUID;

import com.stakevault.betting.bets.domain.model.BettingHouse;
import com.stakevault.betting.bets.domain.model.PagedResult;

public interface BettingHouseRepository {

	BettingHouse save(BettingHouse bettingHouse);

	boolean existsByName(String name);

	boolean existsById(UUID id);

	PagedResult<BettingHouse> findAll(int page, int size);
}
