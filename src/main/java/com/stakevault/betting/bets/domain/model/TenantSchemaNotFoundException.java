package com.stakevault.betting.bets.domain.model;

public class TenantSchemaNotFoundException extends RuntimeException {

	public TenantSchemaNotFoundException(TenantSchemaName schema) {
		super("tenant schema not provisioned: " + schema.value());
	}
}
