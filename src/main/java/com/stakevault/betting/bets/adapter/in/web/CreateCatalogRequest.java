package com.stakevault.betting.bets.adapter.in.web;

import jakarta.validation.constraints.NotBlank;

public record CreateCatalogRequest(@NotBlank String name) {
}
