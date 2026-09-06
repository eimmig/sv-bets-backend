package com.stakevault.betting.bets.domain.port.in;

import java.util.UUID;

import com.stakevault.betting.bets.domain.model.Bet;
import com.stakevault.betting.bets.domain.model.BetFilter;
import com.stakevault.betting.bets.domain.model.BetStatus;
import com.stakevault.betting.bets.domain.model.PagedResult;

public interface BetUseCase {

	BetCreationResult create(CreateBetCommand command);

	Bet findById(UUID id);

	Bet updateStatus(UUID id, BetStatus newStatus, UUID settledByUserId);

	PagedResult<Bet> list(BetFilter filter, int page, int size);
}
