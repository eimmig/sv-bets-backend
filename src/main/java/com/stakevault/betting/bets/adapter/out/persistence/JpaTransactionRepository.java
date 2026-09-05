package com.stakevault.betting.bets.adapter.out.persistence;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.stakevault.betting.bets.domain.model.PagedResult;
import com.stakevault.betting.bets.domain.model.Transaction;
import com.stakevault.betting.bets.domain.port.out.TransactionRepository;

@Repository
public class JpaTransactionRepository implements TransactionRepository {

	private final TransactionSpringDataRepository jpaRepository;

	public JpaTransactionRepository(TransactionSpringDataRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public Transaction save(Transaction transaction) {
		TransactionJpaEntity saved = jpaRepository.save(new TransactionJpaEntity(transaction.id(),
				transaction.bettingHouseId(), transaction.type(), transaction.amount(), transaction.createdAt()));
		return toDomain(saved);
	}

	@Override
	public PagedResult<Transaction> findAll(int page, int size) {
		return toPagedResult(jpaRepository.findAll(PageRequest.of(page, size)));
	}

	@Override
	public PagedResult<Transaction> findByBettingHouseId(UUID bettingHouseId, int page, int size) {
		return toPagedResult(jpaRepository.findByBettingHouseId(bettingHouseId, PageRequest.of(page, size)));
	}

	@Override
	public Map<UUID, BigDecimal> sumNetAmountByBettingHouseIds(Collection<UUID> bettingHouseIds) {
		if (bettingHouseIds.isEmpty()) {
			return Map.of();
		}
		Map<UUID, BigDecimal> netAmountById = new HashMap<>();
		for (Object[] row : jpaRepository.sumNetAmountByBettingHouseIds(bettingHouseIds)) {
			netAmountById.put((UUID) row[0], (BigDecimal) row[1]);
		}
		return netAmountById;
	}

	private static PagedResult<Transaction> toPagedResult(Page<TransactionJpaEntity> result) {
		return new PagedResult<>(result.getContent().stream().map(JpaTransactionRepository::toDomain).toList(),
				result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
	}

	private static Transaction toDomain(TransactionJpaEntity entity) {
		return new Transaction(entity.getId(), entity.getBettingHouseId(), entity.getType(), entity.getAmount(),
				entity.getCreatedAt());
	}
}
