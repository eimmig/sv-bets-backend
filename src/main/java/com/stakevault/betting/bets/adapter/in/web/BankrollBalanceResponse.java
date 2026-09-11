package com.stakevault.betting.bets.adapter.in.web;

import java.math.BigDecimal;

import com.stakevault.betting.bets.domain.model.BankrollBalance;

public record BankrollBalanceResponse(String at, BigDecimal balance) {

	static BankrollBalanceResponse from(BankrollBalance bankrollBalance) {
		return new BankrollBalanceResponse(bankrollBalance.at().toString(), bankrollBalance.balance());
	}
}
