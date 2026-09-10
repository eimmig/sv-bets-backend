-- epic-013: config por tenant, linha unica. Sem pgcrypto/gen_random_uuid() neste codebase - usa
-- UUID constante literal (ver JpaTenantSettingsRepository.ROW_ID), suficiente pra uma linha so.
-- Replay via Flyway lazy (migrateIfPending) seeda automaticamente todo tenant, novo ou existente.
CREATE TABLE tenant_settings (
    id UUID PRIMARY KEY,
    unit_percent NUMERIC(5, 4) NOT NULL DEFAULT 0.01
);

INSERT INTO tenant_settings (id, unit_percent)
VALUES ('00000000-0000-0000-0000-000000000001', 0.01);
