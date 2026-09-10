package com.stakevault.betting.bets.adapter.out.persistence;

import com.stakevault.betting.bets.domain.model.BetType;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class BetTypeAttributeConverter implements AttributeConverter<BetType, String> {

	@Override
	public String convertToDatabaseColumn(BetType betType) {
		return betType == null ? null : betType.name().toLowerCase();
	}

	@Override
	public BetType convertToEntityAttribute(String dbValue) {
		return dbValue == null ? null : BetType.valueOf(dbValue.toUpperCase());
	}
}
