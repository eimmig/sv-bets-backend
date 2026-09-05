package com.stakevault.betting.bets.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stakevault.betting.bets.domain.model.League;
import com.stakevault.betting.bets.domain.port.in.LeagueCatalogUseCase;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/leagues")
public class LeaguesController {

	private static final int MAX_PAGE_SIZE = 100;

	private final LeagueCatalogUseCase leagueCatalog;

	public LeaguesController(LeagueCatalogUseCase leagueCatalog) {
		this.leagueCatalog = leagueCatalog;
	}

	@PostMapping
	public ResponseEntity<CatalogResponse> create(@Valid @RequestBody CreateCatalogRequest request) {
		League league = leagueCatalog.create(request.name());
		return ResponseEntity.status(HttpStatus.CREATED).body(new CatalogResponse(league.id(), league.name()));
	}

	@GetMapping
	public PagedResponse<CatalogResponse> list(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return PagedResponse.from(
				leagueCatalog.list(Math.clamp(page, 0, Integer.MAX_VALUE), Math.clamp(size, 1, MAX_PAGE_SIZE)),
				league -> new CatalogResponse(league.id(), league.name()));
	}
}
