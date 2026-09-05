# Log de Progresso — bets-service

## Estado Atual (Current State)

**Última atualização:** 2026-09-05
**Feature ativa:** nenhuma (`feat-004` `done`, `feat-005`/`feat-006` liberadas)

## Status

### O que está pronto

- [x] Harness deste serviço criado.
- [x] **`feat-001` (Setup do projeto) — `done` em 2026-09-04.** Primeiro código de aplicação do
      serviço. Spring Boot 4.1.1 (Java 25, Maven), layout hexagonal. 9 subtasks (SV-61..69,
      story SV-60). Ver `feature_list.json` (campo `evidence`) para o detalhe completo:
      - Pipeline de CI endurecida **antes** do bootstrap do `pom.xml` (portada do padrão já
        validado em `auth-service` — 6 passos, gate de zero issue do SonarCloud), evitando o
        ciclo de descoberta reativa que `auth-service` passou.
      - Conexão Postgres dev/test/prod, provisionamento de schema de tenant (Flyway lazy) +
        filtro `X-Tenant-Id` + rota admin `POST /api/v1/admin/tenants` (`X-Admin-Api-Key`) —
        bundlada nesta feature (diferente de `auth-service`, feature separada) por decisão do
        Plan Reviewer: aqui a rota só cria schema, sem usuário/senha.
      - Gate JaCoCo 80% (real 97%+), confirmado de verdade (elevado a 0.99 para observar falha,
        revertido).
      - i18n completo desde o início — `MessageSource`, 3 locales, `LocalizedDomainException`,
        e os 2 erros do filtro (`TenantSchemaFilter`/`AdminApiKeyFilter`) localizados de
        verdade, **indo além** do residual permanente aceito em `auth-service` (lá esses 2
        erros nunca saíram de texto hardcoded).
      - Health checks do Actuator, `.env.example` + logging JSON estruturado.
      - Achado real na revisão final: `FilterProblemWriter` (extraído no `/code-review` de
        `feat-001.4`) nunca chamava `setCharacterEncoding("UTF-8")` — gotcha já documentado em
        `docs/CONVENTIONS.md` desde `auth-service feat-003`, quase regredido ao extrair o
        helper compartilhado. Corrigido, reproduzido (teste falha sem a correção, passa com
        ela).
      - 2 achados reais do SonarCloud só visíveis no gate `feature->develop` (mesmo padrão de
        `auth-service` SV-20): teste sem assertion (`S2699`) e `isEqualTo(0)` em vez de
        `isZero()` (`S5838`) — corrigidos.

- [x] **`feat-002` (Catálogos base) — `done` em 2026-09-05.** 4 catálogos (`SPORT`/`LEAGUE`/
      `MARKET`/`TIPSTER`), POST + GET paginado, primeira multi-tenancy do Hibernate do serviço,
      `TenantSchemaFilter` passou a exigir `X-Tenant-Id` em rotas de negócio (fecha o residual de
      `feat-001`). Ver `feature_list.json` (campo `evidence`) para o detalhe completo, incluindo:
      - Bug real corrigido em `feat-002.5`: `PageRequest.of()` lança `IllegalArgumentException`
        (500 não tratado) para `page` negativo ou `size` não positivo — controllers passaram a
        clampar antes de chamar o repositório. Documentado como convenção normativa em
        `docs/API-CONTRACTS.md` (reaproveitável por `feat-007` e `stats-service`).
      - **Achado de processo, não de código**: `feature/SV-70` e as subtasks SV-71..74 nunca
        haviam sido empurradas para o GitHub — os merges de `feat-002.1..4` (sessão anterior)
        foram feitos só localmente (`git merge --no-ff`), sem PR nem gate de CI, violando a regra
        do `CLAUDE.md` raiz ("merge subtask → story exige pipeline de CI do GitHub passando").
        Não reescrito (histórico já mesclado, sem valor em refazer) — só sinalizado. A partir de
        `feat-002.5`, o fluxo correto (push + PR + CI verde + merge `--no-ff`) foi seguido, e
        `feature/SV-70` foi empurrada retroativamente antes do merge de SV-75.

- [x] **`feat-003` (RF03/RF13 — Casas de apostas e movimentações) — `done` em 2026-09-05.**
      `BETTING_HOUSE`/`TRANSACTION`, POST + GET paginado, saldo por casa calculado (RN01, parcela
      pré-liquidação) numa única query agregada por página. Ver `feature_list.json` (campo
      `evidence`) para o detalhe completo, incluindo:
      - Desvio real do plano: `TransactionType` no JSON acabou minúsculo (`@JsonProperty` por
        constante), não maiúsculo como o plano original copiou de `auth-service Role` — o
        precedente de `Role` nunca foi uma convenção deliberada, só o default do Jackson.
        Documentado em `docs/CONVENTIONS.md` para não repetir o erro em `BET.status`.
      - Gotcha real de teste: `BigDecimal.equals()` distingue escala — comparação direta de
        `record` com valor recém-persistido em coluna `NUMERIC(19,2)` falha mesmo com o dado
        correto. Documentado em `docs/TESTING.md`.
      - `AbstractJpaEntity` extraída (boilerplate `id`/`isNew`/`@PostLoad`, antes só em
        `CatalogJpaEntity`) — reaproveitável por `auth-service`/`stats-service`.

- [x] **`feat-004` (RF04/RF12 — Registro e ciclo de vida da aposta) — `done` em 2026-09-05.**
      Entidade `BET` (4 FKs: `bettingHouse`/`sport`/`league`/`market` obrigatórias, `tipster`
      nullable — corrigido no `plan_review` contra `docs/contracts/bet-created.schema.json` já
      existente), `POST /api/v1/bets` (X-User-Id obrigatório, RN07 odd/stake como regra de
      domínio 422, `Idempotency-Key` opcional com replay 200/201), `GET /api/v1/bets/{id}` e
      `PATCH /api/v1/bets/{id}/status` (RF12, só `pending -> won|lost|void` válido). Ver
      `feature_list.json` (campo `evidence`) para o detalhe completo, incluindo:
      - Primeira vez que o serviço faz `UPDATE` de uma linha já persistida (não só `INSERT`) —
        padrão `Persistable`/`isNew` (carregar via `findById`, mutar por método da própria
        entidade, salvar a mesma instância) documentado em `docs/CONVENTIONS.md` para
        `auth-service`/`stats-service` reaproveitarem.
      - Risco residual aceito (TOCTOU): `BetService.updateStatus` sem guarda atômica —
        detalhe na seção "Bloqueios / Riscos" abaixo.

### Em andamento

- Nenhuma feature iniciada.

### Próximos passos (Next Steps)

1. `feat-005` (RF06/RF07 — Processamento de resultado e bankroll consolidado) e `feat-006`
   (Publicação do evento `BetCreated`) estão liberadas (dependem só de `feat-004`, `done`) — WIP
   máximo 1 por serviço, escolher uma.

## Bloqueios / Riscos

- Nenhum bloqueio real.
- **Residual aceito (TOCTOU)**: `AdminProvisionTenantService.provisionTenant()` checa
  `exists()` antes de `ensureSchemaExists()`, sem lock — duas chamadas administrativas
  concorrentes para o mesmo slug novo podem ambas suceder em silêncio (`createAndMigrate` é
  idempotente via `Flyway.createSchemas(true)`), sem nunca retornar `409` para a segunda.
  Diferente de `auth-service`, onde uma constraint `UNIQUE` de `USER` eventualmente pega a
  corrida. Aceito por ser endpoint de baixo volume, uso manual do operador — Persistence
  Auditor confirmou sem risco de corrupção (Flyway usa lock consultivo próprio sobre
  `flyway_schema_history`).
- ~~Residual aceito: `TenantSchemaFilter` passa direto quando `X-Tenant-Id` está ausente~~ —
  **corrigido em `feat-002.2`**: os endpoints de catálogo são a primeira escrita real via
  Hibernate multi-tenant do serviço, então o filtro agora responde `400 missing-tenant-id` em
  qualquer rota de negócio (fora de `/api/v1/admin/**` e `/actuator/**`) sem esperar `feat-004`.
- Cache `migratedSchemas` (em `JdbcFlywayTenantSchemaGateway`) nunca expira — aceitável porque
  cresce por schema-de-tenant-visto-no-processo (cardinalidade = número de organizações
  clientes), não por entidade de alto volume.
- **Residual aceito (TOCTOU, `feat-004`)**: `BetService.updateStatus` lê o status atual
  (`findById`) e só depois escreve (`betRepository.updateStatus`), sem `WHERE status='pending'`
  atômico nem `@Version` — duas chamadas `PATCH /api/v1/bets/{id}/status` concorrentes para a
  mesma aposta `pending` poderiam ambas passar a validação e a segunda sobrescrever a primeira
  (last-write-wins), sem nunca retornar `422`. Persistence Auditor confirmou sem consequência
  hoje (não existe `BET_RESULT`/`profit` associado ainda) — revisitar em `feat-005`, quando essa
  transição precisar ser exatamente-uma-vez para o cálculo de lucro/prejuízo ser confiável.

## Decisões tomadas

- Build tool: **Maven**. Arquitetura: **hexagonal** (domain/application/adapter). Ambas
  decididas em `../../docs/CONVENTIONS.md`, não específicas desta sessão.
- Rota admin de provisionamento de tenant bundlada em `feat-001` (não uma feature separada como
  em `auth-service`) — decisão do Plan Reviewer, aceita como simplificação legítima dado que
  não há criação de usuário/senha aqui.
- i18n retrofit dos erros de filtro (`invalid-tenant-id`, `tenant-not-found`) — decisão tomada
  durante `feat-001.6` de ir além do residual permanente de `auth-service`, já que o mecanismo
  (injetar `MessageSource`/`LocaleResolver` direto no filtro) já estava provado funcionando por
  `AdminApiKeyFilter` de `auth-service`.

## Arquivos modificados nesta sessão (`feat-004`)

- `src/main/resources/db/migration/V20260905224836__create_bet_table.sql`, domínio (`Bet`,
  `BetStatus` + exceções), persistência (`BetJpaEntity`, `JpaBetRepository`,
  `BetStatusAttributeConverter`, `existsById` nos 4 repositórios de catálogo), aplicação
  (`BetService`), web (`BetsController`, DTOs), i18n (3 locales), testes de integração
  (`JpaBetRepositoryIntegrationTest`, `BetsControllerIntegrationTest`), `CLAUDE.md`,
  `feature_list.json`, `CHANGELOG.md`.

## Evidência de conclusão (`feat-004`)

- `./init.sh` (`mvn verify`, JaCoCo 80% incluso) verde localmente com Docker ativo, em cada uma
  das 5 subtasks.
- CI verde em todas as 5 PRs de subtask (`subtask/SV-82..86` → `feature/SV-81`).
- Plan Reviewer (3 MAJOR + 1 MINOR corrigidos no plano), Delivery Reviewer, Test Suite Auditor e
  Persistence Auditor rodados — `PASS` nos três últimos, com 1 achado `CONCERNS` não bloqueante
  (TOCTOU em `updateStatus`, ver "Bloqueios / Riscos"). Detalhe completo em `feature_list.json`
  (campo `evidence` de `feat-004`).

## Notas para a próxima sessão

`feat-005` (RF06/RF07 — bankroll consolidado) e `feat-006` (evento `BetCreated`) estão
liberadas — escolher uma (WIP máximo 1). `feat-005` reage à transição de status introduzida em
`feat-004` (criação de `BET_RESULT`, cálculo de profit RN02/RN03, atualização imediata do saldo
RN05) — ao planejá-la, decidir se o TOCTOU residual de `updateStatus` precisa de correção agora
(guarda atômica `WHERE status='pending'` ou `@Version`) antes de acoplar lógica financeira a essa
transição.
