package com.stakevault.betting.bets.config;

import com.stakevault.betting.bets.domain.model.TenantSchemaName;

public final class TenantContextHolder {

	private static final ThreadLocal<TenantSchemaName> CURRENT = new ThreadLocal<>();

	private TenantContextHolder() {
	}

	static void set(TenantSchemaName schema) {
		CURRENT.set(schema);
	}

	static void clear() {
		CURRENT.remove();
	}

	public static TenantSchemaName current() {
		return CURRENT.get();
	}
}
