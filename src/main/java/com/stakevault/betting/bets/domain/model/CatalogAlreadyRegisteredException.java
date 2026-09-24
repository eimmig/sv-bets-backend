package com.stakevault.betting.bets.domain.model;

public class CatalogAlreadyRegisteredException extends LocalizedRuntimeException {

	private final String catalogSlug;

	public CatalogAlreadyRegisteredException(String catalogSlug, String name) {
		super(catalogSlug + " already registered: " + name, name == null ? "" : name);
		this.catalogSlug = catalogSlug;
	}

	@Override
	public String messageKey() {
		return "error." + catalogSlug + "-already-registered";
	}

	@Override
	public int httpStatusCode() {
		return 409;
	}
}
