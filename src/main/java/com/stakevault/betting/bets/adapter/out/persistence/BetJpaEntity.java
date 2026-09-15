package com.stakevault.betting.bets.adapter.out.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.stakevault.betting.bets.domain.model.Bet;
import com.stakevault.betting.bets.domain.model.BetStatus;
import com.stakevault.betting.bets.domain.model.BetType;

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

	@Column(name = "team1_id")
	private UUID team1Id;

	@Column(name = "team2_id")
	private UUID team2Id;

	private String description;

	@Column(name = "bet_type")
	private BetType betType;

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

	public BetJpaEntity(Bet bet) {
		super(bet.id());
		this.bettingHouseId = bet.bettingHouseId();
		this.sportId = bet.sportId();
		this.leagueId = bet.leagueId();
		this.marketId = bet.marketId();
		this.tipsterId = bet.tipsterId();
		this.createdByUserId = bet.createdByUserId();
		this.ticketNumber = bet.ticketNumber();
		this.team1Id = bet.team1Id();
		this.team2Id = bet.team2Id();
		this.description = bet.description();
		this.betType = bet.betType();
		this.playType = bet.playType();
		this.stake = bet.stake();
		this.odd = bet.odd();
		this.status = bet.status();
		this.betDate = bet.betDate();
		this.idempotencyKey = bet.idempotencyKey();
	}
}
