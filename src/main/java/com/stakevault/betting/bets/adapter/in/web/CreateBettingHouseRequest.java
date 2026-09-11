package com.stakevault.betting.bets.adapter.in.web;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateBettingHouseRequest(@NotBlank String name, @NotNull @PositiveOrZero BigDecimal initialBalance) {
}
