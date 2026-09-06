package com.stakevault.betting.bets.domain.port.out;

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
}
