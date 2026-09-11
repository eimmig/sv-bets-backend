package com.stakevault.betting.bets.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum TransactionType {
	@JsonProperty("deposit")
	DEPOSIT,
	@JsonProperty("withdrawal")
	WITHDRAWAL
}
