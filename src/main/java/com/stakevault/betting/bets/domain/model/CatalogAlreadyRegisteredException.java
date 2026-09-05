package com.stakevault.betting.bets.domain.model;

public class CatalogAlreadyRegisteredException extends RuntimeException implements LocalizedDomainException {

	private final String catalogSlug;
	private final String name;

	public CatalogAlreadyRegisteredException(String catalogSlug, String name) {
		super(catalogSlug + " already registered: " + name);
		this.catalogSlug = catalogSlug;
		this.name = name;
	}

	@Override
	public String messageKey() {
		return "error." + catalogSlug + "-already-registered";
	}

	@Override
	public int httpStatusCode() {
		return 409;
	}

	@Override
	public Object[] messageArgs() {
		return new Object[] { name == null ? "" : name };
	}
}
