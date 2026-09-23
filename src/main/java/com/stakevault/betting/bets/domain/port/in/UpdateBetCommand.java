package com.stakevault.betting.bets.domain.port.in;

import com.stakevault.betting.bets.domain.model.BetStatus;

public record UpdateBetCommand(BetDetails details, BetStatus status) {

	public UpdateBetCommand(BetFields fields, BetStatus status) {
		this(BetDetails.of(fields), status);
	}
}
