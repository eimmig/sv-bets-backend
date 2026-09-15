ALTER TABLE bet
    ADD COLUMN team1_id UUID REFERENCES team (id),
    ADD COLUMN team2_id UUID REFERENCES team (id);

ALTER TABLE bet
    DROP COLUMN team1,
    DROP COLUMN team2;
