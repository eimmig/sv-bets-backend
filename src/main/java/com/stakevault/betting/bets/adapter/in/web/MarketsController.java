package com.stakevault.betting.bets.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stakevault.betting.bets.domain.model.Market;
import com.stakevault.betting.bets.domain.port.in.MarketCatalogUseCase;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/markets")
public class MarketsController {

	private static final int MAX_PAGE_SIZE = 100;

	private final MarketCatalogUseCase marketCatalog;

	public MarketsController(MarketCatalogUseCase marketCatalog) {
		this.marketCatalog = marketCatalog;
	}

	@PostMapping
	public ResponseEntity<CatalogResponse> create(@Valid @RequestBody CreateCatalogRequest request) {
		Market market = marketCatalog.create(request.name());
		return ResponseEntity.status(HttpStatus.CREATED).body(new CatalogResponse(market.id(), market.name()));
	}

	@GetMapping
	public PagedResponse<CatalogResponse> list(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return PagedResponse.from(
				marketCatalog.list(Math.clamp(page, 0, Integer.MAX_VALUE), Math.clamp(size, 1, MAX_PAGE_SIZE)),
				market -> new CatalogResponse(market.id(), market.name()));
	}
}
