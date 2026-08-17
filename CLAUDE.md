# CLAUDE.md — bets-service

Casas de apostas, apostas, bankroll, movimentações e publicação dos eventos `BetCreated`/
`BetSettled`. Java 25 + Spring Boot 4.x. Parte do harness multinível do monorepo — leia `../../CLAUDE.md`
(raiz) para invariantes cross-service antes deste arquivo, e
`../../docs/services/bets-service.md` para o desenho completo (modelo de dados, RN01–RN09).
Arquitetura interna, build tool, testes e formato de API são normativos e já decididos em
`../../docs/CONVENTIONS.md`, `../../docs/TESTING.md` e `../../docs/API-CONTRACTS.md` — leia-os
antes de `feat-001`.

## Fluxo de início de sessão (Startup Workflow)

1. Confirme o diretório de trabalho (`pwd`) — deve ser `services/bets-service`.
2. Leia `../../CLAUDE.md` e `../../docs/services/bets-service.md`.
3. Rode `./init.sh` para verificar build/testes deste serviço.
4. Leia `feature_list.json` (deste serviço) para a próxima feature granular.
5. Leia `progress.md` (deste serviço).

## Regras específicas deste serviço

- **Uma feature por vez (One feature at a time)**: escolha exatamente uma feature `not-started`
  de `feature_list.json` cujas dependências já estejam `done`.
- **Escopo restrito (stay in scope)**: não edite código de outro serviço a partir desta pasta,
  mesmo o `stats-service` que consome o evento publicado aqui.
- Multi-tenancy por **schema isolado por tenant** (Schema-per-Tenant, `tenant_<slug>`) dentro do
  banco Postgres deste serviço — não confundir com Database per Service (isso já separa este
  banco dos outros serviços; o schema-per-tenant é uma camada adicional dentro deste banco).
  Schema resolvido a partir do header `X-Tenant-Id` (ver bullet abaixo — **não** `X-User-Id`) e
  migrado sob demanda (Flyway lazy) antes de atender a requisição — ver
  `../../docs/CONVENTIONS.md` seção "Migrations". Exponha também uma rota administrativa que cria
  o schema deste serviço quando um tenant novo é provisionado, autenticada por
  `X-Admin-Api-Key` (ver `../../docs/API-CONTRACTS.md`) — chamada manualmente pelo operador,
  **não** por `auth-service` em código (decisão de 2026-08-02, ver `../../docs/DECISIONS-LOG.md`
  item 3: 3 chamadas manuais separadas, `auth-service` primeiro, este serviço depois).
- Regras de negócio RN01–RN03, RN05–RN07 (ver `../../docs/REQUIREMENTS.md`) são normativas:
  implemente exatamente os cálculos e validações descritos, não aproxime.
- O endpoint `POST /api/v1/bets` é usado tanto pelo formulário web (`apps/web`) quanto pelo
  `services/telegram-integration` (via `api-gateway`) — não crie um endpoint separado para a
  captura automática. Aceita header `Idempotency-Key` (ver `../../docs/API-CONTRACTS.md`). Todas
  as rotas deste serviço (`/api/v1/betting-houses`, `/api/v1/bets`, `/api/v1/transactions`) e
  seus query params são sempre em inglês — ver `../../docs/API-CONTRACTS.md`.
- Campo `status` de `BET` (e nos eventos) usa valores em inglês: `pending`/`won`/`lost`/`void`
  (correspondem a pendente/ganha/perdida/devolvida em RF12/RN06 — a especificação em si, ver
  `../../docs/REQUIREMENTS.md`, continua em português; só a codificação técnica é inglês).
- Publique **dois eventos distintos**, não um único evento reaproveitado: `BetCreated` no
  registro inicial (`../../docs/contracts/bet-created.schema.json`) e `BetSettled` na
  liquidação (`../../docs/contracts/bet-settled.schema.json`) — ver
  `../../docs/API-CONTRACTS.md`. Mudanças de payload em qualquer um dos dois atualizam o schema
  correspondente, `../../docs/services/bets-service.md` e `../../docs/services/stats-service.md`
  no mesmo commit.
- **Maven** (não Gradle) e **arquitetura hexagonal** (`domain/`, `application/`, `adapter/`) —
  decisões já tomadas em `../../docs/CONVENTIONS.md`, não reabrir. O publicador do evento vive
  em `adapter/out/messaging/`, implementando um `port/out` do domínio.
- Erros de API em `application/problem+json` (RFC 7807) — ver `../../docs/API-CONTRACTS.md`,
  com `title`/`detail` localizados por `Accept-Language` (`pt-BR`/`en-US`/`es` sempre em
  sincronia, ver `../../docs/CONVENTIONS.md` seção "Internacionalização (i18n)").
- Confia nos headers `X-User-Id` (quem chamou) e `X-Tenant-Id` (organização/schema, injetados
  pelo API Gateway) — **`X-Tenant-Id` é a identidade usada para resolver o schema da conexão**,
  não `X-User-Id` (os dois deixaram de ser o mesmo valor em 2026-08-02, ver
  `../../docs/DECISIONS-LOG.md`). Não revalida o token PASETO aqui (ver
  `../../docs/API-CONTRACTS.md`).
- `BET.createdByUserId` e `BET_RESULT.settledByUserId` gravam `X-User-Id` como trilha de
  auditoria (decisão de 2026-08-02) — não são FK reais (`USER` vive no banco `auth`, outro
  serviço), só o `uuid` copiado do header. O envelope de `BetCreated`/`BetSettled` também carrega
  `userId` além de `tenantId` — ver `../../docs/contracts/*.schema.json` e
  `../../docs/DECISIONS-LOG.md`.
- Não modele `USER` aqui (pertence a `services/auth-service`) nem `FACT_BET`/dimensões OLAP
  (pertence a `services/stats-service`).
- **CI/CD (`feat-009`)**: pipeline em `.github/workflows/ci.yml`, **dentro deste repositório**
  (este serviço é seu próprio repositório Git, não um monorepo — ver
  `../../docs/DECISIONS-LOG.md` "Topologia") — changelog, i18n, build, testes, SonarCloud.
  Scripts de validação em `.github/scripts/` (duplicados aqui, não compartilhados com os outros
  serviços). Ver `../../docs/CI-CD.md`. Toda feature adiciona uma entrada em `CHANGELOG.md`
  deste serviço (verificado automaticamente pelo CI quando este repositório existir no GitHub).
- **Skills de agente prioritárias**: `Plan Reviewer` antes de codificar, `Delivery Reviewer` +
  `Test Suite Auditor` + `Persistence Auditor` (banco próprio, schema-per-tenant) antes de
  marcar `done` (claude-code-skills) — mapeamento completo em `../../docs/AGENT-SKILLS.md`.
  Instaladas em 2026-08-02 (escopo `user`), ver `../../docs/DECISIONS-LOG.md`.

## Definição de pronto (Definition of Done)

Uma feature deste serviço só está `done` quando (done only when):

> **Antes de começar** (não é item de `done`, é pré-requisito de `in-progress`): o campo
> `plan_review` daquela feature em `feature_list.json` precisa estar preenchido com o
> resultado do `Plan Reviewer` — ver `CLAUDE.md` da raiz, seção "Regras de trabalho".


- [ ] Implementada e rodando via `./init.sh` sem erro (`mvn verify`, gate de cobertura incluso).
- [ ] Regras de negócio relevantes (RN01–RN09) cobertas por teste (ver `../../docs/TESTING.md`),
      incluindo i18n (`title`/`detail` de erro mudam por `Accept-Language`).
- [ ] Se a feature publica/altera `BetCreated` ou `BetSettled`: teste validando a mensagem
      contra o JSON Schema correspondente em `../../docs/contracts/`.
- [ ] `Delivery Reviewer`, `Test Suite Auditor` e `Persistence Auditor` rodados contra a feature
      (ver `../../docs/AGENT-SKILLS.md`).
- [ ] `CHANGELOG.md` deste serviço tem uma entrada em `[Unreleased]` descrevendo a mudança.
- [ ] `feature_list.json` atualizado com status e evidência.
- [ ] `../../feature_list.json` (raiz) atualizado se este foi o marco que fecha `epic-003`.

## Fim de sessão (End of Session)

Antes de encerrar (before ending a session): atualize `progress.md` deste serviço, atualize
`feature_list.json`, e deixe `./init.sh` passando (clean, restartable state) — stay in scope:
os eventos `BetCreated`/`BetSettled` são a única superfície que pode tocar outro serviço
(indiretamente, via contrato); não edite código de `stats-service` nesta pasta.

## Verificação

```bash
./init.sh
```
