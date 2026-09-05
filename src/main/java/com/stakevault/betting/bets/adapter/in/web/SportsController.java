package com.stakevault.betting.bets.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stakevault.betting.bets.domain.model.Sport;
import com.stakevault.betting.bets.domain.port.in.SportCatalogUseCase;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/sports")
public class SportsController {

	private static final int MAX_PAGE_SIZE = 100;

	private final SportCatalogUseCase sportCatalog;

	public SportsController(SportCatalogUseCase sportCatalog) {
		this.sportCatalog = sportCatalog;
	}

	@PostMapping
	public ResponseEntity<CatalogResponse> create(@Valid @RequestBody CreateCatalogRequest request) {
		Sport sport = sportCatalog.create(request.name());
		return ResponseEntity.status(HttpStatus.CREATED).body(new CatalogResponse(sport.id(), sport.name()));
	}

	@GetMapping
	public PagedResponse<CatalogResponse> list(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return PagedResponse.from(sportCatalog.list(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE)),
				sport -> new CatalogResponse(sport.id(), sport.name()));
	}
}
