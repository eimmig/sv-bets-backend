package com.stakevault.betting.bets.adapter.out.persistence;

import com.stakevault.betting.bets.domain.model.TransactionType;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TransactionTypeAttributeConverter implements AttributeConverter<TransactionType, String> {

	@Override
	public String convertToDatabaseColumn(TransactionType type) {
		return type == null ? null : type.name().toLowerCase();
	}

	@Override
	public TransactionType convertToEntityAttribute(String dbValue) {
		return dbValue == null ? null : TransactionType.valueOf(dbValue.toUpperCase());
	}
}
