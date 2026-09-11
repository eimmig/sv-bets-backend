package com.stakevault.betting.bets.adapter.in.web;

import java.time.LocalDate;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stakevault.betting.bets.domain.port.in.BankrollUseCase;

@RestController
@RequestMapping("/api/v1/bankroll")
public class BankrollController {

	private final BankrollUseCase bankroll;

	public BankrollController(BankrollUseCase bankroll) {
		this.bankroll = bankroll;
	}

	@GetMapping("/balance")
	public BankrollBalanceResponse getBalance(@RequestParam(required = false) LocalDate at) {
		return BankrollBalanceResponse.from(bankroll.getBalance(at));
	}
}
