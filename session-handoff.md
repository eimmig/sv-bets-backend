# Session Handoff — bets-service

> Estado atual, não histórico. O diário cronológico é o `progress.md` — este arquivo é reescrito
> a cada sessão para responder "o que a próxima sessão precisa saber agora".

**Última atualização:** 2026-09-15

## Objetivo atual

`feat-001`..`feat-018` `done`. Backlog deste serviço esgotado — nenhuma feature `not-started`
elegível agora. `main` já promovido (PR #70) — `develop -> main` aconteceu de propósito para
provar o job `deploy` de `feat-018`, e a prova revelou um problema real de infraestrutura (ver
abaixo), não um bug de código.

## Concluído nesta sessão (2026-09-15)

- [x] `feat-018` fechada (CD automático). `apps/web feat-021` corrigiu depois a quebra de contrato
      que motivava adiar a promoção. Ver `progress.md` para o detalhe de ambos.
- [x] **`develop -> main` promovido (PR #70), primeiro disparo real do job `deploy` — FALHOU por
      infraestrutura, não por código**: `build-and-push-image` funcionou (imagem nova no GHCR);
      `kubectl rollout restart` falhou com `connection refused` em `127.0.0.1:6443`. Causa raiz:
      o `KUBE_CONFIG` distribuído por `infra/feat-007` capturou o `server:` do túnel SSH local que
      o usuário tinha aberto no momento de gerar a credencial (`ssh -L 6443:127.0.0.1:6443 ...`),
      não um endereço alcançável pelo runner hospedado do GitHub Actions. **Nenhum dano ao
      cluster** — o comando nunca chegou a se conectar, o `Deployment` em produção continua com a
      imagem antiga. Detalhe completo, incluindo os 3 caminhos possíveis de correção (nenhum
      decidido — decisão de rede do usuário), em `docs/services/infra.md` "CD automático via CI".

## Bloqueios / Riscos

- **Bloqueio real, não deste serviço especificamente**: o mecanismo de `KUBE_CONFIG` de
  `epic-028` não funciona a partir de runners hospedados do GitHub Actions — mesma falha esperada
  nos outros 5 repositórios (`stats-service`, `api-gateway`, `auth-service`,
  `telegram-integration`, `web`), todos com o mesmo secret gerado na mesma sessão. **Promoções
  `develop -> main` dos outros 5 pausadas de propósito** até o usuário decidir o caminho de rede
  (regenerar certificado TLS do k3s com SAN alcançável + expor a porta / runner self-hosted na
  rede local / túnel-relay tipo Tailscale — ver `docs/services/infra.md` para o detalhe).
- O rollout manual (`kubectl apply`/`rollout restart` rodado pelo usuário via túnel SSH, mesmo
  padrão de `infra/feat-006`) continua funcionando normalmente — só a automação via CI que não
  alcança o cluster.

## Próxima sessão — por onde começar

1. Rodar `./init.sh` (precisa de Docker rodando, Testcontainers).
2. Backlog deste serviço vazio.
3. **Não repetir a promoção `develop -> main` dos outros 5 repositórios de `epic-028` até o
   usuário decidir o caminho de rede** — vai falhar da mesma forma. Se quiser rodar o
   `kubectl rollout restart` de `bets-service` manualmente enquanto isso, mesmo padrão de sempre
   (túnel SSH + comando direto).
