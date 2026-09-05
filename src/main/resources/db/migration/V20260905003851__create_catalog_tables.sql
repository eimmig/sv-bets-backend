CREATE TABLE sport (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT uq_sport_name UNIQUE (name)
);

CREATE TABLE league (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT uq_league_name UNIQUE (name)
);

CREATE TABLE market (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT uq_market_name UNIQUE (name)
);

CREATE TABLE tipster (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT uq_tipster_name UNIQUE (name)
);
