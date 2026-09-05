CREATE TABLE betting_house (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    initial_balance NUMERIC(19, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_betting_house_name UNIQUE (name)
);

CREATE TABLE transaction (
    id UUID PRIMARY KEY,
    betting_house_id UUID NOT NULL REFERENCES betting_house (id),
    type VARCHAR(20) NOT NULL CHECK (type IN ('deposit', 'withdrawal')),
    amount NUMERIC(19, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL
);
