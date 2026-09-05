package com.stakevault.betting.bets.adapter.in.web;

import com.stakevault.betting.bets.domain.model.BetStatus;

import jakarta.validation.constraints.NotNull;

public record UpdateBetStatusRequest(@NotNull BetStatus status) {
}
