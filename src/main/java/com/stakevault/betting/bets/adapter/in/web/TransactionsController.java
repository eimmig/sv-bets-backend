package com.stakevault.betting.bets.adapter.in.web;

import java.time.Instant;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stakevault.betting.bets.domain.port.in.TransactionUseCase;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionsController {

	private static final int MAX_PAGE_SIZE = 100;

	private final TransactionUseCase transactions;

	public TransactionsController(TransactionUseCase transactions) {
		this.transactions = transactions;
	}

	@PostMapping
	public ResponseEntity<TransactionResponse> create(@Valid @RequestBody CreateTransactionRequest request) {
		var created = transactions.create(request.bettingHouseId(), request.type(), request.amount());
		return ResponseEntity.status(HttpStatus.CREATED).body(TransactionResponse.from(created));
	}

	@GetMapping
	public PagedResponse<TransactionResponse> list(
			@RequestParam(required = false) UUID bettingHouseId,
			@RequestParam(required = false) Instant from,
			@RequestParam(required = false) Instant to,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return PagedResponse.from(
				transactions.list(bettingHouseId, from, to, Math.clamp(page, 0, Integer.MAX_VALUE),
						Math.clamp(size, 1, MAX_PAGE_SIZE)),
				TransactionResponse::from);
	}
}
