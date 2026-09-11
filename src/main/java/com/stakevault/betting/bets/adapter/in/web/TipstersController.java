package com.stakevault.betting.bets.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stakevault.betting.bets.domain.model.Tipster;
import com.stakevault.betting.bets.domain.port.in.TipsterCatalogUseCase;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/tipsters")
public class TipstersController {

	private static final int MAX_PAGE_SIZE = 100;

	private final TipsterCatalogUseCase tipsterCatalog;

	public TipstersController(TipsterCatalogUseCase tipsterCatalog) {
		this.tipsterCatalog = tipsterCatalog;
	}

	@PostMapping
	public ResponseEntity<CatalogResponse> create(@Valid @RequestBody CreateCatalogRequest request) {
		Tipster tipster = tipsterCatalog.create(request.name());
		return ResponseEntity.status(HttpStatus.CREATED).body(new CatalogResponse(tipster.id(), tipster.name()));
	}

	@GetMapping
	public PagedResponse<CatalogResponse> list(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return PagedResponse.from(
				tipsterCatalog.list(Math.clamp(page, 0, Integer.MAX_VALUE), Math.clamp(size, 1, MAX_PAGE_SIZE)),
				tipster -> new CatalogResponse(tipster.id(), tipster.name()));
	}
}
