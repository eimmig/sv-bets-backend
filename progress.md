# Log de Progresso — bets-service

## Estado Atual (Current State)

**Última atualização:** 2026-09-06
**Feature ativa:** nenhuma (`feat-007` `done` — todas as features de negócio de `bets-service`
concluídas; só `feat-009`, formalidade de CI, resta no backlog)

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
        **corrigido em `feat-005.3`**, ver abaixo.

- [x] **`feat-005` (RF06/RF07 — Processamento de resultado e bankroll consolidado) — `done` em
      2026-09-06.** `BET_RESULT` (1:1 com `BET`), `PATCH /api/v1/bets/{id}/status` passou a
      exigir `X-User-Id`, transição de status virou `UPDATE` atômico condicional (corrige o
      TOCTOU de `feat-004`), profit calculado (RN02/RN03) e persistido na mesma transação
      (`@Transactional`, primeira vez real no serviço), `GET /api/v1/betting-houses` soma esse
      profit no `balance` (RN01/RN05). Ver `feature_list.json` (campo `evidence`) para o detalhe
      completo, incluindo:
      - Teste de concorrência real (2 threads via `ExecutorService` contra o servidor HTTP,
        `WebEnvironment.RANDOM_PORT`) prova que só uma de duas liquidações simultâneas para a
        mesma aposta sucede — a outra recebe `422`, nunca dado corrompido.
      - `docs/CONVENTIONS.md` distingue agora dois padrões de update: `findById`+mutar+`save`
        para updates incondicionais (`feat-004`), `@Modifying @Query` atômico para transições de
        estado com guarda de negócio (`feat-005`) — reaproveitável por `auth-service`/
        `stats-service`.

- [x] **`feat-006` (Publicação do evento `BetCreated`) — `done` em 2026-09-06.** Primeira
      mensageria do serviço: `spring-boot-starter-amqp` + Testcontainers RabbitMQ,
      `RabbitBetEventPublisher` publica `BetCreated` (exchange `bets.events`, routing key
      `bet.created`) logo após o `INSERT` suceder, mensagem `PERSISTENT`, falha de publish logada
      e nunca propagada como erro HTTP (sem outbox/retry — risco residual aceito). Ver
      `feature_list.json` (campo `evidence`) para o detalhe completo, incluindo:
      - Cópia vendorizada de `docs/contracts/bet-created.schema.json` em
        `src/test/resources/contracts/` — o schema mora no repositório `sv-harness`, que a CI
        deste repositório não faz checkout; achado do Plan Reviewer, evita um teste que passa
        local e falha sempre no CI real.
      - `com.networknt:json-schema-validator` pinado em `1.5.9` (não a versão mais recente,
        `3.0.7` — reescrita completa da API, descoberta só na compilação do teste).
      - `MessageProperties.getDeliveryMode()` retorna `null` numa mensagem RECEBIDA — o valor
        real fica em `getReceivedDeliveryMode()`; achado real na revisão final (mensagem não
        marcada `PERSISTENT`), corrigido e coberto por teste antes de `done`.

- [x] **`feat-008` (Publicação do evento `BetSettled`) — `done` em 2026-09-06.** Reaproveitou
      quase integralmente o mecanismo de `feat-006` (mesma sessão) — `BetEventEnvelope`,
      `RabbitBetEventPublisher`, `PERSISTENT`, log-e-engole em falha de publish. Publica após a
      transição atômica e o `BET_RESULT` serem salvos com sucesso (nunca em transição inválida,
      `422`). Payload confirmado contra o schema real como **diferente** de `BetCreated` — sem os
      campos descritivos de `BET`. Plan Reviewer: `READY`, sem achados novos (gotchas de
      `feat-006` não reincidiram). Ver `feature_list.json` (campo `evidence`) para o detalhe
      completo.

- [x] **`feat-007` (RF08 — Histórico paginado de apostas e movimentações) — `done` em
      2026-09-06.** `GET /api/v1/bets` (novo, listagem paginada com filtros
      `bettingHouseId`/`sportId`/`leagueId`/`marketId`/`tipsterId`/`from`/`to`) e
      `GET /api/v1/transactions` (ganhou `from`/`to`) — query JPQL única por endpoint com
      predicado condicional por filtro. Ver `feature_list.json` (campo `evidence`) para o
      detalhe completo, incluindo:
      - **Achado real** (só em execução real contra Postgres, não na compilação):
        `(:param IS NULL OR coluna >= :param)` — já usado sem problema para colunas `UUID` desde
        `feat-003`/`feat-004` — quebra para colunas `timestamp` (`could not determine data type
        of parameter`). Corrigido com `coluna >= COALESCE(:param, coluna)`, seguro por as colunas
        (`bet_date`/`created_at`) serem `NOT NULL`. Documentado em `docs/CONVENTIONS.md` para
        `auth-service`/`stats-service`.
      - `docs/API-CONTRACTS.md` corrigido: o exemplo de nomenclatura de filtro (`?sport=football`,
        sem sufixo `Id`) nunca bateu com o precedente real já enviado (`bettingHouseId`, feat-003)
        — a nota estava desatualizada, não o código; corrigida para refletir a nomenclatura real.

**Todas as features de negócio de `bets-service` estão `done`** — resta só `feat-009`
(formalidade de pipeline de CI, já rodando de verdade desde `epic-009`/`feat-001`, mesmo padrão
de fechamento que `auth-service feat-007`).

### Em andamento

- Nenhuma feature iniciada.

### Próximos passos (Next Steps)

1. `feat-009` (Pipeline de CI) é a única feature restante em `bets-service` — fechamento formal,
   sem código novo esperado (mesmo padrão de `auth-service feat-007`: confirmar que a `description`
   da feature bate com o `ci.yml` real, corrigir se tiver ficado desatualizada).

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
- ~~Residual aceito (TOCTOU, `feat-004`): `BetService.updateStatus` lê o status atual e só depois
  escreve, sem guarda atômica~~ — **corrigido em `feat-005.3`**: transição de status agora é um
  `UPDATE` atômico condicional (`WHERE status='pending'`, via `@Modifying @Query`), provado por
  teste de concorrência real (2 threads via `ExecutorService`) — só uma de duas liquidações
  simultâneas para a mesma aposta sucede, a outra recebe `422 invalid-status-transition`.
- **Residual aceito (`feat-006`)**: `RabbitBetEventPublisher.publishCreated` não tem
  outbox/retry — se o broker estiver indisponível no instante do `INSERT`, a falha é logada e a
  aposta é criada normalmente, mas o evento `BetCreated` correspondente nunca chega ao
  `stats-service` (replay de `Idempotency-Key` não tenta republicar). Aceito por ser fora do
  escopo decidido para este projeto (sem mecanismo de outbox); revisitar se o volume/criticidade
  justificar depois.

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

## Arquivos modificados nesta sessão (`feat-007`)

- `BetFilter` (novo record), `BetSpringDataRepository`/`JpaBetRepository.findFiltered`,
  `TransactionSpringDataRepository`/`JpaTransactionRepository.findFiltered` (substitui
  `findAll`/`findByBettingHouseId`), `BetUseCase.list`/`BetService.list`,
  `TransactionUseCase.list`/`TransactionService.list` (ganha `from`/`to`), `BetsController`
  (novo `GET /api/v1/bets`), `TransactionsController` (`from`/`to`), testes
  (`BetsControllerIntegrationTest`, `TransactionsControllerIntegrationTest`,
  `JpaTransactionRepositoryIntegrationTest`), `CLAUDE.md`, `docs/services/bets-service.md`,
  `docs/API-CONTRACTS.md`, `docs/CONVENTIONS.md`, `feature_list.json`, `CHANGELOG.md`.

## Evidência de conclusão (`feat-007`)

- `./init.sh` (`mvn verify`, JaCoCo 80% incluso) verde localmente com Docker ativo, em cada uma
  das 4 subtasks (após corrigir o achado real do `COALESCE`, ver acima).
- CI verde em todas as 4 PRs de subtask (`subtask/SV-103..106` → `feature/SV-102`).
- Plan Reviewer (1 MAJOR corrigido no plano), Delivery Reviewer, Test Suite Auditor e Persistence
  Auditor rodados — `PASS` nos três, achado real do Postgres (`COALESCE`) corrigido e testado
  antes de `done`. Detalhe completo em `feature_list.json` (campo `evidence` de `feat-007`).

## Notas para a próxima sessão

**Todas as features de negócio de `bets-service` estão `done`** (`feat-001` a `feat-008`). Só
resta `feat-009` (pipeline de CI) — fechamento formal sem código novo esperado, mesmo padrão de
`auth-service feat-007`: confirmar que a `description` da feature bate com o `ci.yml` real
(passos, `projectKey`) e corrigir se tiver ficado desatualizada, já que o pipeline roda de
verdade desde `epic-009`/`feat-001` desta sessão. Fechar `feat-009` fecha também `epic-003` na
raiz (`../../feature_list.json`) — atualizar lá também.
