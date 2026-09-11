-- feat-014.3: GET /api/v1/bankroll/balance introduziu agregados que filtram por range de tempo
-- (createdAt/settledAt) sem groupar por betting_house_id - sem indice, cada chamada varre a
-- tabela inteira.
CREATE INDEX idx_transaction_created_at ON transaction (created_at);
CREATE INDEX idx_bet_result_settled_at ON bet_result (settled_at);
