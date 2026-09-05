package com.stakevault.betting.bets.domain.port.out;

import java.util.Optional;
import java.util.UUID;

import com.stakevault.betting.bets.domain.model.Bet;

public interface BetRepository {

	Bet save(Bet bet);

	Optional<Bet> findById(UUID id);

	Optional<Bet> findByIdempotencyKey(String idempotencyKey);
}
