# Session Handoff — bets-service

> Estado atual, não histórico. O diário cronológico é o `progress.md` — este arquivo é reescrito
> a cada sessão para responder "o que a próxima sessão precisa saber agora".

**Última atualização:** 2026-09-15

## Objetivo atual

`feat-001`..`feat-018` `done`. Backlog deste serviço esgotado — nenhuma feature `not-started`
elegível agora. `epic-028` da raiz segue `in-progress` (5 dos 6 repositórios de aplicação ainda
pendentes: `auth-service feat-016`, `stats-service feat-019`, `api-gateway feat-014`,
`telegram-integration feat-010`, `web feat-030`), mas o lado deste repositório está fechado.

## Concluído nesta sessão (2026-09-15)

- [x] **`feat-018` fechada** (CD automático — job `deploy` em `ci.yml`, `kubectl rollout restart
      deployment/bets-service` contra `KUBE_CONFIG`/`ci-deployer` de `infra/feat-007`). Story
      SV-423, subtasks SV-424/SV-425, PRs #67/#68/#69, CI+SonarCloud verdes, merge
      `feature/SV-423 -> develop` concluído. `Plan Reviewer` corrigiu 2 MINOR antes de codificar
      (remover `azure/setup-kubectl` — `kubectl` já vem preinstalado no runner `ubuntu-latest` —
      e adicionar `permissions: {}` explícito, já que o job não usa `GITHUB_TOKEN` e o repositório
      tem `default_workflow_permissions=write`). `Delivery Reviewer`: PASS. `Test Suite Auditor`:
      PASS/N/A (mudança isolada a workflow, sem oráculo unitário significativo para
      `kubectl rollout restart` — a única prova credível é a execução real em CI).
- [x] **Disparo real do job adiado deliberadamente** — não é bug, é decisão desta sessão: `main`
      deste repositório está (era, antes desta feature) 35 commits atrás de `develop`, incluindo
      `feat-017` (quebra conhecida e já documentada do contrato REST síncrono de
      `POST /api/v1/bets` para quem ainda envia `team1`/`team2` como texto livre — `apps/web` só
      corrige em `feat-021`, ainda `not-started`). Promover `develop -> main` agora só para provar
      o job `deploy` forçaria essa quebra em produção sem necessidade — o guard do job já foi
      provado (roda `skipping` corretamente em evento de PR, runs 35007759577/35008325709/
      35008820978), e o `Delivery Reviewer` classificou a falta de disparo real como risco
      residual aceitável, não bloqueante.

## Bloqueios / Riscos

- **Nenhum bloqueio deste serviço.** Risco já documentado (e agora materializado): `main` deste
  repositório ainda não tem `feat-016`/`feat-017`/`feat-018` — a promoção `develop -> main` deste
  repositório deveria esperar `apps/web feat-021` (troca dos inputs de texto livre por
  `team1Id`/`team2Id`) para não quebrar `POST /api/v1/bets` vindo do formulário web em produção.
  **Quando essa promoção finalmente acontecer**, é também a primeira execução real do job
  `deploy` desta feature — registrar a confirmação (log do GitHub Actions com o rollout de
  verdade) em `docs/services/infra.md` nessa ocasião, não deixar como lacuna silenciosa.

## Próxima sessão — por onde começar

1. Rodar `./init.sh` (precisa de Docker rodando, Testcontainers).
2. Backlog deste serviço vazio — não há feature `not-started` elegível aqui. Trabalhar noutro
   harness (WIP máximo 1 por serviço, paralelismo entre serviços permitido) — ver
   `feature_list.json` da raiz para epics elegíveis (`epic-020`/`epic-027` em `apps/web`, demais
   5 repositórios de `epic-028`).
3. Se/quando `apps/web feat-021` fechar: promover este repositório `develop -> main` é o passo
   que faltava para o item acima (log real do job `deploy`).
