package com.stakevault.betting.bets.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum BetType {
	@JsonProperty("pre")
	PRE,
	@JsonProperty("live")
	LIVE
}
