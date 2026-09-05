package com.stakevault.betting.bets.adapter.in.web;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stakevault.betting.bets.domain.model.MissingCallerContextException;
import com.stakevault.betting.bets.domain.port.in.BetCreationResult;
import com.stakevault.betting.bets.domain.port.in.BetUseCase;
import com.stakevault.betting.bets.domain.port.in.CreateBetCommand;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/bets")
public class BetsController {

	public static final String CALLER_HEADER = "X-User-Id";
	public static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

	private final BetUseCase bets;

	public BetsController(BetUseCase bets) {
		this.bets = bets;
	}

	@PostMapping
	public ResponseEntity<BetResponse> create(
			@RequestHeader(value = CALLER_HEADER, required = false) String callerIdHeader,
			@RequestHeader(value = IDEMPOTENCY_KEY_HEADER, required = false) String idempotencyKey,
			@Valid @RequestBody CreateBetRequest request) {
		UUID callerId = parseCallerId(callerIdHeader);

		CreateBetCommand command = new CreateBetCommand(callerId, request.bettingHouseId(), request.sportId(),
				request.leagueId(), request.marketId(), request.tipsterId(), request.ticketNumber(), request.team1(),
				request.team2(), request.description(), request.betType(), request.playType(), request.stake(),
				request.odd(), request.betDate(), idempotencyKey);

		BetCreationResult result = bets.create(command);
		HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
		return ResponseEntity.status(status).body(BetResponse.from(result.bet()));
	}

	private UUID parseCallerId(String callerIdHeader) {
		if (callerIdHeader == null || callerIdHeader.isBlank()) {
			throw new MissingCallerContextException();
		}
		try {
			return UUID.fromString(callerIdHeader);
		} catch (IllegalArgumentException _) {
			throw new MissingCallerContextException();
		}
	}
}
