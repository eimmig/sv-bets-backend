package com.stakevault.betting.bets.adapter.out.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface BetResultSpringDataRepository extends JpaRepository<BetResultJpaEntity, UUID> {

	Optional<BetResultJpaEntity> findByBetId(UUID betId);

	// bet_result has no betting_house_id column - implicit join to BetJpaEntity by id, same
	// style as TransactionSpringDataRepository.sumNetAmountByBettingHouseIds.
	@Query("SELECT b.bettingHouseId, SUM(r.profit) FROM BetResultJpaEntity r, BetJpaEntity b "
			+ "WHERE r.betId = b.id AND b.bettingHouseId IN :bettingHouseIds GROUP BY b.bettingHouseId")
	List<Object[]> sumProfitByBettingHouseIds(@Param("bettingHouseIds") Collection<UUID> bettingHouseIds);
}
