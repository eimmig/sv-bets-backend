package com.stakevault.betting.bets.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stakevault.betting.bets.domain.model.Team;
import com.stakevault.betting.bets.domain.port.in.TeamCatalogUseCase;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/teams")
public class TeamsController {

	private static final int MAX_PAGE_SIZE = 100;

	private final TeamCatalogUseCase teamCatalog;

	public TeamsController(TeamCatalogUseCase teamCatalog) {
		this.teamCatalog = teamCatalog;
	}

	@PostMapping
	public ResponseEntity<TeamResponse> create(@Valid @RequestBody CreateTeamRequest request) {
		Team team = teamCatalog.create(request.name(), request.sportId());
		return ResponseEntity.status(HttpStatus.CREATED).body(TeamResponse.from(team));
	}

	@GetMapping
	public PagedResponse<TeamResponse> list(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return PagedResponse.from(
				teamCatalog.list(Math.clamp(page, 0, Integer.MAX_VALUE), Math.clamp(size, 1, MAX_PAGE_SIZE)),
				TeamResponse::from);
	}
}
