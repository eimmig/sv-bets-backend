# Session Handoff — bets-service

> Estado atual, não histórico. O diário cronológico é o `progress.md` — este arquivo é reescrito
> a cada sessão para responder "o que a próxima sessão precisa saber agora".

**Última atualização:** 2026-09-15

## Objetivo atual

`feat-001`..`feat-016` `done`. `feat-016` (avaliação TEAM/PLAYER, `epic-024` da raiz) fechou
nesta sessão — feature de decisão, sem código de produção alterado. `feat-017` (implementação
real do catálogo `TEAM`) criada no backlog, `not-started`, sem `plan_review` ainda.

## Concluído nesta sessão (2026-09-15)

- [x] `feat-016` fechada (4 subtasks, story SV-407, PR #63). Decisão 1: `TEAM` vira catálogo
      escopado por esporte, chave natural `(name, sportId)` — mesma chave já provada em produção
      por `stats-service`/`dim_team`. Decisão 2: `PLAYER` fica fora desta rodada (decisão
      delegada ao agente pelo usuário). Plano de contratos completo em
      `docs/DECISIONS-LOG.md` (raiz). Ver `progress.md` para o detalhe completo.

## Bloqueios / Riscos

Nenhum.

## Próxima sessão — por onde começar

1. Rodar `./init.sh` (precisa de Docker rodando, Testcontainers).
2. `feat-017` (implementar catálogo `TEAM`) está no backlog, `not-started`, sem `plan_review` —
   rodar o Plan Reviewer antes de codificar, como de costume. A decisão de design já está
   registrada em `docs/DECISIONS-LOG.md` (2026-09-15) — não redecidir a chave natural nem reabrir
   a discussão de `PLAYER` sem novo pedido do usuário.
3. `feat-017` desbloqueia `apps/web feat-021` (tela de cadastro de time, hoje `BLOCKED` no
   `plan_review` esperando exatamente esta decisão) e `stats-service feat-018` (ainda sem
   `plan_review`).
