CREATE TABLE team (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    sport_id UUID NOT NULL REFERENCES sport (id),
    CONSTRAINT uq_team_name_sport UNIQUE (name, sport_id)
);
