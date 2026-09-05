package com.stakevault.betting.bets.adapter.out.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.stakevault.betting.bets.domain.model.BetStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bet")
@Getter
@NoArgsConstructor
public class BetJpaEntity extends AbstractJpaEntity {

	@Column(name = "betting_house_id", nullable = false)
	private UUID bettingHouseId;

	@Column(name = "sport_id", nullable = false)
	private UUID sportId;

	@Column(name = "league_id", nullable = false)
	private UUID leagueId;

	@Column(name = "market_id", nullable = false)
	private UUID marketId;

	@Column(name = "tipster_id")
	private UUID tipsterId;

	@Column(name = "created_by_user_id", nullable = false)
	private UUID createdByUserId;

	@Column(name = "ticket_number")
	private String ticketNumber;

	private String team1;

	private String team2;

	private String description;

	@Column(name = "bet_type")
	private String betType;

	@Column(name = "play_type")
	private String playType;

	@Column(nullable = false)
	private BigDecimal stake;

	@Column(nullable = false)
	private BigDecimal odd;

	@Column(nullable = false)
	private BetStatus status;

	@Column(name = "bet_date", nullable = false)
	private Instant betDate;

	@Column(name = "idempotency_key")
	private String idempotencyKey;

	public BetJpaEntity(UUID id, UUID bettingHouseId, UUID sportId, UUID leagueId, UUID marketId, UUID tipsterId,
			UUID createdByUserId, String ticketNumber, String team1, String team2, String description, String betType,
			String playType, BigDecimal stake, BigDecimal odd, BetStatus status, Instant betDate, String idempotencyKey) {
		super(id);
		this.bettingHouseId = bettingHouseId;
		this.sportId = sportId;
		this.leagueId = leagueId;
		this.marketId = marketId;
		this.tipsterId = tipsterId;
		this.createdByUserId = createdByUserId;
		this.ticketNumber = ticketNumber;
		this.team1 = team1;
		this.team2 = team2;
		this.description = description;
		this.betType = betType;
		this.playType = playType;
		this.stake = stake;
		this.odd = odd;
		this.status = status;
		this.betDate = betDate;
		this.idempotencyKey = idempotencyKey;
	}

	void updateStatus(BetStatus status) {
		this.status = status;
	}
}
