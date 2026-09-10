package com.stakevault.betting.bets.domain.port.in;

import java.time.LocalDate;

import com.stakevault.betting.bets.domain.model.BankrollBalance;

public interface BankrollUseCase {

	BankrollBalance getBalance(LocalDate at);
}
