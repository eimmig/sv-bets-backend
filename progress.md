# Log de Progresso — bets-service

## Estado Atual (Current State)

**Última atualização:** 2026-09-04
**Feature ativa:** nenhuma (`feat-001` `done`, `feat-002` liberada)

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

### Em andamento

- Nenhuma feature iniciada.

### Próximos passos (Next Steps)

1. `feat-002` (Catálogos base — `SPORT`/`LEAGUE`/`MARKET`/`TIPSTER`) é a próxima feature
   liberada — primeira a introduzir entidades JPA reais e, portanto, a multi-tenancy do
   Hibernate (`CurrentTenantIdentifierResolver`/`MultiTenantConnectionProvider`, mecanismo já
   documentado em `docs/CONVENTIONS.md` a partir de `auth-service feat-002.5`).

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
- **Residual aceito**: `TenantSchemaFilter` passa direto (`chain.doFilter`) quando
  `X-Tenant-Id` está ausente, sem enforcement — nenhuma rota de negócio desta feature exige o
  header ainda. Revisitar quando `feat-004`/RF04 (registro de aposta) existir e realmente
  precisar do header.
- Cache `migratedSchemas` (em `JdbcFlywayTenantSchemaGateway`) nunca expira — aceitável porque
  cresce por schema-de-tenant-visto-no-processo (cardinalidade = número de organizações
  clientes), não por entidade de alto volume.

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

## Arquivos modificados nesta sessão

- Todo o esqueleto do serviço: `pom.xml`, `src/main/**`, `src/test/**`, `.env.example`,
  `.github/workflows/ci.yml`, `.github/scripts/validate-sonar-issues.py`, `CHANGELOG.md`,
  `feature_list.json`.

## Evidência de conclusão

- `./init.sh` (`mvn verify`, JaCoCo 80% incluso) verde localmente com Docker ativo — 34 testes,
  0 falhas, 97%+ cobertura de linha real.
- CI verde em todos os 9 PRs (8 de subtask + 1 de fechamento da story), incluindo o gate
  completo (SonarCloud + GitGuardian) na PR `feature/SV-60` → `develop`.
- Plan Reviewer, Delivery Reviewer, Test Suite Auditor e Persistence Auditor rodados — `PASS`
  nos quatro (Plan Reviewer com 1 MAJOR corrigido no plano). Detalhe completo em
  `feature_list.json` (campo `evidence` de `feat-001`).

## Notas para a próxima sessão

`feat-002` (Catálogos base) é a próxima feature — `Plan Reviewer` antes de codificar, mesmo
fluxo já validado ponta a ponta em `feat-001`. Primeira feature a introduzir JPA/Hibernate
multi-tenancy — reaproveitar o mecanismo já documentado em `docs/CONVENTIONS.md`
(`auth-service feat-002.5`), confirmando as chaves de configuração via `javap` contra o jar
instalado antes de assumir de memória (podem ter mudado de versão).
