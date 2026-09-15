package com.stakevault.betting.bets.adapter.in.web;

import java.util.UUID;

import com.stakevault.betting.bets.domain.model.Team;

public record TeamResponse(UUID id, String name, UUID sportId) {

	static TeamResponse from(Team team) {
		return new TeamResponse(team.id(), team.name(), team.sportId());
	}
}
