package com.stakevault.betting.bets.application;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.stereotype.Service;

import com.stakevault.betting.bets.domain.model.BankrollBalance;
import com.stakevault.betting.bets.domain.port.in.BankrollUseCase;
import com.stakevault.betting.bets.domain.port.out.BetResultRepository;
import com.stakevault.betting.bets.domain.port.out.BettingHouseRepository;
import com.stakevault.betting.bets.domain.port.out.TransactionRepository;

@Service
public class BankrollService implements BankrollUseCase {

	// docs/CONVENTIONS.md "Timezone padrao" - so o corte de dia civil usa o fuso do Brasil,
	// armazenamento continua Instant/UTC.
	private static final ZoneId BRAZIL_ZONE = ZoneId.of("America/Sao_Paulo");

	private final BettingHouseRepository bettingHouseRepository;
	private final TransactionRepository transactionRepository;
	private final BetResultRepository betResultRepository;

	public BankrollService(BettingHouseRepository bettingHouseRepository, TransactionRepository transactionRepository,
			BetResultRepository betResultRepository) {
		this.bettingHouseRepository = bettingHouseRepository;
		this.transactionRepository = transactionRepository;
		this.betResultRepository = betResultRepository;
	}

	@Override
	public BankrollBalance getBalance(LocalDate at) {
		LocalDate day = at != null ? at : LocalDate.now(BRAZIL_ZONE);
		Instant exclusiveUpperBound = day.plusDays(1).atStartOfDay(BRAZIL_ZONE).toInstant();

		var balance = bettingHouseRepository.sumInitialBalance()
				.add(transactionRepository.sumNetAmountUpTo(exclusiveUpperBound))
				.add(betResultRepository.sumProfitUpTo(exclusiveUpperBound));

		return new BankrollBalance(day, balance);
	}
}
