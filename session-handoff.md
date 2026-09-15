# Session Handoff — bets-service

> Estado atual, não histórico. O diário cronológico é o `progress.md` — este arquivo é reescrito
> a cada sessão para responder "o que a próxima sessão precisa saber agora".

**Última atualização:** 2026-09-15

## Objetivo atual

`feat-001`..`feat-017` `done`. Nenhuma feature elegível neste serviço agora — o único item do
backlog, `feat-018` (CD: job de deploy automático `kubectl rollout restart`), depende de
`infra/feat-007` (ServiceAccount de CI + kubeconfig), ainda `not-started` naquele repositório.

## Concluído nesta sessão (2026-09-15)

- [x] `feat-017` fechada (5 subtasks, story SV-412, PR #66 merged em `develop`). Catálogo `TEAM`
      escopado por esporte (`POST`/`GET /api/v1/teams`); `Bet.team1`/`team2` migrados de texto
      livre para `team1Id`/`team2Id` (FK). Contrato de evento mantido aditivo (`Plan Reviewer`
      corrigiu o plano original antes de codificar — ver `progress.md` para o achado completo).
      `Delivery Reviewer` achou um segundo risco real (contrato REST síncrono não é aditivo,
      `apps/web` vai quebrar até `feat-021` de lá trocar os inputs) — documentado, não bloqueante
      pra fechar esta feature.

## Bloqueios / Riscos

- **Nenhum bloqueio deste serviço.** Risco externo documentado: não fazer deploy deste `develop`
  (quando `epic-028`/CD automático existir) para um ambiente com usuário real de `apps/web` antes
  de `apps/web feat-021` estar pronta — `POST /api/v1/bets` vindo do formulário atual vai
  devolver 400 (ver `docs/services/bets-service.md` seção "Registro e ciclo de vida da aposta").

## Próxima sessão — por onde começar

1. Rodar `./init.sh` (precisa de Docker rodando, Testcontainers).
2. Nenhuma feature elegível aqui até `infra/feat-007` fechar (desbloqueia `feat-018`, CD). Se
   `infra/feat-007` já estiver `done`, `feat-018` pode começar — plan_review próprio, ainda vazio.
3. Considerar trabalhar num harness diferente enquanto isso (WIP máximo 1 por serviço, paralelismo
   entre serviços permitido) — ver `feature_list.json` da raiz para epics elegíveis.
