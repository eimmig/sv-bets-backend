package com.stakevault.betting.bets.adapter.in.web;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateTeamRequest(@NotBlank String name, @NotNull UUID sportId) {
}
