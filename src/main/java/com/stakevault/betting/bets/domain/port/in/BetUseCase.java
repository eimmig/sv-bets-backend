package com.stakevault.betting.bets.domain.port.in;

public interface BetUseCase {

	BetCreationResult create(CreateBetCommand command);
}
