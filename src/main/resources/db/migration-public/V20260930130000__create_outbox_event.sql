CREATE TABLE outbox_event (
    id           BIGSERIAL PRIMARY KEY,
    routing_key  VARCHAR(64) NOT NULL,
    payload      TEXT NOT NULL,
    tenant_slug  VARCHAR(56) NOT NULL,
    bet_id       UUID NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL
) WITH (autovacuum_vacuum_scale_factor = 0, autovacuum_vacuum_threshold = 5000);
