package com.stakevault.betting.bets.adapter.out.persistence;

import com.stakevault.betting.bets.domain.model.BetStatus;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class BetStatusAttributeConverter implements AttributeConverter<BetStatus, String> {

	@Override
	public String convertToDatabaseColumn(BetStatus status) {
		return status == null ? null : status.name().toLowerCase();
	}

	@Override
	public BetStatus convertToEntityAttribute(String dbValue) {
		return dbValue == null ? null : BetStatus.valueOf(dbValue.toUpperCase());
	}
}
