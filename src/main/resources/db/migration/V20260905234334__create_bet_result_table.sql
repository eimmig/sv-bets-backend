CREATE TABLE bet_result (
    id UUID PRIMARY KEY,
    bet_id UUID NOT NULL REFERENCES bet(id),
    settled_by_user_id UUID NOT NULL,
    profit NUMERIC(19, 2) NOT NULL,
    settled_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_bet_result_bet_id UNIQUE (bet_id)
);
