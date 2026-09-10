package com.stakevault.betting.bets.domain.model;

public class AdminRoleRequiredException extends RuntimeException implements LocalizedDomainException {

	public AdminRoleRequiredException() {
		super("caller does not have the admin role");
	}

	@Override
	public String messageKey() {
		return "error.admin-role-required";
	}

	@Override
	public int httpStatusCode() {
		return 403;
	}
}
