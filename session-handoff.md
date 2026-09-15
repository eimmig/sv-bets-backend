# Session Handoff — bets-service

> Estado atual, não histórico. O diário cronológico é o `progress.md` — este arquivo é reescrito
> a cada sessão para responder "o que a próxima sessão precisa saber agora".

**Última atualização:** 2026-09-15

## Objetivo atual

`feat-001`..`feat-018` `done`. Backlog deste serviço esgotado — nenhuma feature `not-started`
elegível agora. `epic-028` da raiz fechou por completo nesta mesma sessão (6 de 6 repositórios).

## Concluído nesta sessão (2026-09-15)

- [x] **`feat-018` fechada** (CD automático — job `deploy` em `ci.yml`). Ver detalhe completo em
      `progress.md`.
- [x] **Disparo real do job adiado deliberadamente** por uma razão concreta: `main` deste
      repositório não tinha `feat-017`, que quebra o contrato REST síncrono de `POST /api/v1/bets`
      para quem ainda envia `team1`/`team2` como texto livre — `apps/web` só corrigia isso em
      `feat-021`, então `not-started`.
- [x] **A razão acima deixou de existir na mesma sessão** — a pedido explícito do usuário
      ("corrija isso primeiro" antes de qualquer deploy em massa), `apps/web feat-021` fechou
      depois: `register-bet.ts`/`.html` trocou os 2 inputs de texto livre por selects
      `team1Id`/`team2Id`, alimentados por um catálogo de times novo (`shared/team-manager`) —
      ver `apps/web/progress.md` para o detalhe completo daquela feature. `POST /api/v1/bets`
      vindo do formulário web não quebra mais.

## Bloqueios / Riscos

Nenhum. A promoção `develop -> main` deste repositório já pode acontecer sem o risco antes
documentado aqui — é também a oportunidade de confirmar o job `deploy` de `feat-018` rodando de
verdade pela primeira vez (registrar o log do GitHub Actions em `docs/services/infra.md` nessa
ocasião).

## Próxima sessão — por onde começar

1. Rodar `./init.sh` (precisa de Docker rodando, Testcontainers).
2. Backlog deste serviço vazio — não há feature `not-started` elegível aqui. Trabalhar noutro
   harness (WIP máximo 1 por serviço, paralelismo entre serviços permitido).
3. Promover `develop -> main` deste repositório é seguro agora (a quebra de contrato que motivava
   adiar foi corrigida em `apps/web feat-021`) — ao fazê-lo, confirmar e registrar o job `deploy`
   rodando de verdade pela primeira vez.
