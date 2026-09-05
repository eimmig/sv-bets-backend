package com.stakevault.betting.bets.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import com.stakevault.betting.bets.config.TenantContextScope;
import com.stakevault.betting.bets.domain.model.BettingHouse;
import com.stakevault.betting.bets.domain.model.PagedResult;
import com.stakevault.betting.bets.domain.model.Transaction;
import com.stakevault.betting.bets.domain.model.TransactionType;
import com.stakevault.betting.bets.domain.port.in.ProvisionTenantSchemaUseCase;
import com.stakevault.betting.bets.domain.port.out.BettingHouseRepository;
import com.stakevault.betting.bets.domain.port.out.TransactionRepository;
import com.stakevault.betting.bets.support.TenantSchemaIntegrationSupport;

class JpaTransactionRepositoryIntegrationTest extends TenantSchemaIntegrationSupport {

	private final TransactionRepository transactionRepository;
	private final BettingHouseRepository bettingHouseRepository;

	JpaTransactionRepositoryIntegrationTest(ProvisionTenantSchemaUseCase provisionTenantSchema, JdbcTemplate jdbcTemplate,
			TransactionRepository transactionRepository, BettingHouseRepository bettingHouseRepository) {
		super(provisionTenantSchema, jdbcTemplate);
		this.transactionRepository = transactionRepository;
		this.bettingHouseRepository = bettingHouseRepository;
	}

	private UUID newBettingHouseId() {
		BettingHouse bettingHouse = new BettingHouse(UUID.randomUUID(), "House-" + UUID.randomUUID(), BigDecimal.ZERO,
				Instant.now());
		return bettingHouseRepository.save(bettingHouse).id();
	}

	@Test
	void shouldSaveAndListInSamePage() {
		try (var _ = TenantContextScope.open(schema)) {
			UUID bettingHouseId = newBettingHouseId();
			Transaction transaction = new Transaction(UUID.randomUUID(), bettingHouseId, TransactionType.DEPOSIT,
					BigDecimal.valueOf(50), Instant.now());

			transactionRepository.save(transaction);

			PagedResult<Transaction> page = transactionRepository.findAll(0, 20);

			assertThat(page.content()).extracting(Transaction::id).contains(transaction.id());
		}
	}

	@Test
	void findByBettingHouseIdShouldFilterOnlyThatHouse() {
		try (var _ = TenantContextScope.open(schema)) {
			UUID houseA = newBettingHouseId();
			UUID houseB = newBettingHouseId();

			transactionRepository.save(new Transaction(UUID.randomUUID(), houseA, TransactionType.DEPOSIT,
					BigDecimal.TEN, Instant.now()));
			transactionRepository.save(new Transaction(UUID.randomUUID(), houseB, TransactionType.DEPOSIT,
					BigDecimal.ONE, Instant.now()));

			PagedResult<Transaction> houseAPage = transactionRepository.findByBettingHouseId(houseA, 0, 20);

			assertThat(houseAPage.content()).extracting(Transaction::bettingHouseId).containsOnly(houseA);
		}
	}

	@Test
	void sumNetAmountByBettingHouseIdsShouldSubtractWithdrawalsFromDeposits() {
		try (var _ = TenantContextScope.open(schema)) {
			UUID houseWithMovements = newBettingHouseId();
			UUID houseWithoutMovements = newBettingHouseId();

			transactionRepository.save(new Transaction(UUID.randomUUID(), houseWithMovements, TransactionType.DEPOSIT,
					BigDecimal.valueOf(100), Instant.now()));
			transactionRepository.save(new Transaction(UUID.randomUUID(), houseWithMovements, TransactionType.DEPOSIT,
					BigDecimal.valueOf(50), Instant.now()));
			transactionRepository.save(new Transaction(UUID.randomUUID(), houseWithMovements, TransactionType.WITHDRAWAL,
					BigDecimal.valueOf(30), Instant.now()));

			Map<UUID, BigDecimal> netAmountById = transactionRepository
					.sumNetAmountByBettingHouseIds(List.of(houseWithMovements, houseWithoutMovements));

			assertThat(netAmountById.get(houseWithMovements)).isEqualByComparingTo("120");
			assertThat(netAmountById).doesNotContainKey(houseWithoutMovements);
		}
	}

	@Test
	void sumNetAmountByBettingHouseIdsShouldReturnEmptyMapForEmptyInput() {
		try (var _ = TenantContextScope.open(schema)) {
			assertThat(transactionRepository.sumNetAmountByBettingHouseIds(List.of())).isEmpty();
		}
	}
}
