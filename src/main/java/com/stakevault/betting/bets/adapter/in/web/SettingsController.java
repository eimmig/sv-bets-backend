package com.stakevault.betting.bets.adapter.in.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stakevault.betting.bets.domain.model.AdminRoleRequiredException;
import com.stakevault.betting.bets.domain.port.in.TenantSettingsUseCase;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/settings")
public class SettingsController {

	public static final String ROLE_HEADER = "X-User-Role";
	private static final String ADMIN_ROLE = "admin";

	private final TenantSettingsUseCase tenantSettings;

	public SettingsController(TenantSettingsUseCase tenantSettings) {
		this.tenantSettings = tenantSettings;
	}

	@GetMapping
	public TenantSettingsResponse get() {
		return TenantSettingsResponse.from(tenantSettings.get());
	}

	@PatchMapping
	public TenantSettingsResponse update(@RequestHeader(value = ROLE_HEADER, required = false) String role,
			@Valid @RequestBody UpdateTenantSettingsRequest request) {
		if (!ADMIN_ROLE.equals(role)) {
			throw new AdminRoleRequiredException();
		}
		return TenantSettingsResponse.from(tenantSettings.updateUnitPercent(request.unitPercent()));
	}
}
