package com.stakevault.betting.bets.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stakevault.betting.bets.domain.port.in.BettingHouseUseCase;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/betting-houses")
public class BettingHousesController {

	private static final int MAX_PAGE_SIZE = 100;

	private final BettingHouseUseCase bettingHouses;

	public BettingHousesController(BettingHouseUseCase bettingHouses) {
		this.bettingHouses = bettingHouses;
	}

	@PostMapping
	public ResponseEntity<BettingHouseResponse> create(@Valid @RequestBody CreateBettingHouseRequest request) {
		var created = bettingHouses.create(request.name(), request.initialBalance());
		return ResponseEntity.status(HttpStatus.CREATED).body(BettingHouseResponse.from(created));
	}

	@GetMapping
	public PagedResponse<BettingHouseResponse> list(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return PagedResponse.from(
				bettingHouses.list(Math.clamp(page, 0, Integer.MAX_VALUE), Math.clamp(size, 1, MAX_PAGE_SIZE)),
				BettingHouseResponse::from);
	}
}
