package com.stakevault.betting.bets.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.stakevault.betting.bets.domain.model.BettingHouseNotFoundException;
import com.stakevault.betting.bets.domain.model.PagedResult;
import com.stakevault.betting.bets.domain.model.Transaction;
import com.stakevault.betting.bets.domain.model.TransactionType;
import com.stakevault.betting.bets.domain.port.in.TransactionUseCase;
import com.stakevault.betting.bets.domain.port.out.BettingHouseRepository;
import com.stakevault.betting.bets.domain.port.out.TransactionRepository;

@Service
public class TransactionService implements TransactionUseCase {

	private final TransactionRepository transactionRepository;
	private final BettingHouseRepository bettingHouseRepository;

	public TransactionService(TransactionRepository transactionRepository, BettingHouseRepository bettingHouseRepository) {
		this.transactionRepository = transactionRepository;
		this.bettingHouseRepository = bettingHouseRepository;
	}

	@Override
	public Transaction create(UUID bettingHouseId, TransactionType type, BigDecimal amount) {
		if (!bettingHouseRepository.existsById(bettingHouseId)) {
			throw new BettingHouseNotFoundException(bettingHouseId);
		}
		return transactionRepository.save(new Transaction(UUID.randomUUID(), bettingHouseId, type, amount, Instant.now()));
	}

	@Override
	public PagedResult<Transaction> list(UUID bettingHouseId, Instant from, Instant to, int page, int size) {
		return transactionRepository.findFiltered(bettingHouseId, from, to, page, size);
	}
}
