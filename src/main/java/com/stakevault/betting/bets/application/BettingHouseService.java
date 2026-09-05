package com.stakevault.betting.bets.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.stakevault.betting.bets.domain.model.BettingHouse;
import com.stakevault.betting.bets.domain.model.BettingHouseAlreadyRegisteredException;
import com.stakevault.betting.bets.domain.model.BettingHouseBalance;
import com.stakevault.betting.bets.domain.model.PagedResult;
import com.stakevault.betting.bets.domain.port.in.BettingHouseUseCase;
import com.stakevault.betting.bets.domain.port.out.BettingHouseRepository;
import com.stakevault.betting.bets.domain.port.out.TransactionRepository;

@Service
public class BettingHouseService implements BettingHouseUseCase {

	private final BettingHouseRepository bettingHouseRepository;
	private final TransactionRepository transactionRepository;

	public BettingHouseService(BettingHouseRepository bettingHouseRepository, TransactionRepository transactionRepository) {
		this.bettingHouseRepository = bettingHouseRepository;
		this.transactionRepository = transactionRepository;
	}

	@Override
	public BettingHouseBalance create(String name, BigDecimal initialBalance) {
		if (bettingHouseRepository.existsByName(name)) {
			throw new BettingHouseAlreadyRegisteredException(name);
		}
		BettingHouse bettingHouse;
		try {
			bettingHouse = bettingHouseRepository
					.save(new BettingHouse(UUID.randomUUID(), name, initialBalance, Instant.now()));
		} catch (DataIntegrityViolationException _) {
			throw new BettingHouseAlreadyRegisteredException(name);
		}
		return new BettingHouseBalance(bettingHouse, initialBalance);
	}

	@Override
	public PagedResult<BettingHouseBalance> list(int page, int size) {
		PagedResult<BettingHouse> result = bettingHouseRepository.findAll(page, size);
		List<UUID> ids = result.content().stream().map(BettingHouse::id).toList();
		Map<UUID, BigDecimal> netAmountById = transactionRepository.sumNetAmountByBettingHouseIds(ids);

		List<BettingHouseBalance> content = result.content().stream()
				.map(bettingHouse -> new BettingHouseBalance(bettingHouse,
						bettingHouse.initialBalance().add(netAmountById.getOrDefault(bettingHouse.id(), BigDecimal.ZERO))))
				.toList();

		return new PagedResult<>(content, result.page(), result.size(), result.totalElements(), result.totalPages());
	}
}
