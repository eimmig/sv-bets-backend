package com.stakevault.betting.bets.domain.port.in;

import java.util.UUID;

public record CreateBetCommand(UUID callerId, BetDetails details, String idempotencyKey) {

	public CreateBetCommand(UUID callerId, BetFields fields, String idempotencyKey) {
		this(callerId, BetDetails.of(fields), idempotencyKey);
	}
}
