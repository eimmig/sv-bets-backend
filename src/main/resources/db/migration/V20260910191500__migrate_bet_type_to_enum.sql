-- epic-013: bet_type migra de texto livre pra enum PRE/LIVE. Linhas existentes com valor fora do
-- dominio (a coluna nunca teve CHECK ate agora) nao tem remapeamento seguro - viram NULL, nao
-- erro (decisao do usuario).
UPDATE bet SET bet_type = lower(bet_type) WHERE bet_type IS NOT NULL;
UPDATE bet SET bet_type = NULL WHERE bet_type NOT IN ('pre', 'live');

ALTER TABLE bet
    ADD CONSTRAINT chk_bet_bet_type CHECK (bet_type IN ('pre', 'live'));
