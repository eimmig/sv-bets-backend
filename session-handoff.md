# Session Handoff — bets-service

> Estado atual, não histórico. O diário cronológico é o `progress.md` — este arquivo é reescrito
> a cada sessão para responder "o que a próxima sessão precisa saber agora".

**Última atualização:** 2026-09-23

## Objetivo atual

`feat-001`..`feat-020` `done`. Backlog deste serviço esgotado — nenhuma feature `not-started`
elegível agora.

## Concluído nesta sessão (2026-09-23)

- [x] **`feat-020` fechada** (Reformulação de marca StakeVault -> Arka, continuação do `epic-032`
      da raiz - 2º dos 4 serviços Java, mesmo plano base de `auth-service feat-019`). Único ponto
      real de marca: `pom.xml` linha 15 (`<description>`). Plan Reviewer condensado (READY) +
      Delivery Reviewer (PASS). Story SV-552, PRs #72-74, CI+SonarCloud verdes.
- [x] Achado de processo corrigido antes de abrir o PR: `develop` tinha 1 commit local nunca
      publicado (`feat-019`, `PUT /api/v1/bets/{id}`, de sessão anterior) - sincronizado
      (fast-forward) antes de ramificar, pra não vazar aquele commit alheio no diff.
- [x] Mesmo residual de ambiente (processos `java.exe` órfãos travando o `repackage` local do
      Maven no Windows, mesmo lock nos 4 serviços Java) documentado em
      `services/auth-service/progress.md` - `mvn test` local verde, `mvn verify` completo
      confirmado pelo CI (Linux).

## Bloqueios / Riscos

- **Bloqueio real ainda não resolvido, não deste serviço especificamente**: o mecanismo de
  `KUBE_CONFIG` de `epic-028` não funciona a partir de runners hospedados do GitHub Actions (capturou
  o endereço de um túnel SSH local, não alcançável pelo runner). **Promoções `develop -> main` dos
  6 repositórios de aplicação continuam pausadas** até o usuário decidir o caminho de rede
  (regenerar certificado TLS do k3s com SAN alcançável + expor a porta / runner self-hosted na
  rede local / túnel-relay tipo Tailscale — ver `docs/services/infra.md` "CD automático via CI"
  pro detalhe completo). O rollout manual (túnel SSH + comando direto) continua funcionando.

## Próxima sessão — por onde começar

1. Rodar `./init.sh` (precisa de Docker rodando, Testcontainers).
2. Backlog deste serviço vazio.
3. **Não promover `develop -> main`** até o usuário decidir o caminho de rede do `KUBE_CONFIG`
   (ver "Bloqueios" acima) — vai falhar da mesma forma.
