# Session Handoff — bets-service

## Current Objective

- Goal: `feat-001` (project setup) — done. `feat-002` (Catálogos base) is next.
- Current status: bootstrap fully shipped and merged into `develop`.
- Branch / commit: `develop` (merge of `feature/SV-60`, story SV-60, 9 subtasks).

## Completed This Session

- [x] `feat-001` fully implemented across 9 subtasks (SV-61..69) — see `progress.md` and the
      `evidence` field of `feat-001` in `feature_list.json` for full detail.
- [x] CI pipeline hardened proactively (before the `pom.xml` bootstrap), porting the already-
      validated pattern from `auth-service` — 6 steps, SonarCloud zero-issue gate included.
      Avoided the reactive discover-then-fix cycle `auth-service` went through.
- [x] i18n retrofit going further than `auth-service`'s own precedent: both filter-level errors
      (`TenantSchemaFilter`, `AdminApiKeyFilter`) resolve real localized messages instead of
      staying permanently hardcoded pt-BR text.
- [x] Real bug found and fixed in the final Delivery Review: `FilterProblemWriter` (extracted
      mid-feature to deduplicate RFC 7807 body construction) never set the UTF-8 charset
      explicitly — a near-regression of a gotcha already documented in `docs/CONVENTIONS.md`
      since `auth-service feat-003`. Reproduced (test fails without the fix) before fixing.
- [x] 2 SonarCloud findings only visible on the `feature->develop` gate (same pattern as
      `auth-service` SV-20) fixed: missing test assertion, `isEqualTo(0)` vs `isZero()`.

## Verification Evidence

| Check | Command | Result | Notes |
|---|---|---|---|
| Build/test | `./init.sh` | exit 0 | 34 tests, 0 failures, JaCoCo 80% gate: 97%+ actual, Docker required (Testcontainers). |
| CI | GitHub Actions, all 10 PRs | green | Includes SonarCloud + GitGuardian on the final story→develop PR. |

## Files Changed

- Entire service skeleton: `pom.xml`, `src/main/java/com/stakevault/betting/bets/**`
  (`domain/model/{TenantSchemaName,TenantSchemaNotFoundException,TenantAlreadyProvisionedException,
  InvalidTenantSlugException,InvalidAdminApiKeyException,LocalizedDomainException}`,
  `domain/port/{in,out}/*`, `application/{ProvisionTenantSchemaService,AdminProvisionTenantService}`,
  `adapter/out/persistence/JdbcFlywayTenantSchemaGateway`, `adapter/in/web/*` (2 filters, admin
  controller, exception handler, `FilterProblemWriter`, `ProblemDetailMessages`),
  `config/{TenantContextHolder,TenantContextScope,LocaleConfig}`).
- `src/main/resources/{application.yml,messages*.properties}`, `.env.example`.
- `.github/workflows/ci.yml`, `.github/scripts/validate-sonar-issues.py` (new).
- Full test suite (34 tests) under `src/test/java/...` + `src/test/resources/junit-platform.properties`.

## Decisions Made

- See `progress.md` "Decisões tomadas" — admin route bundled into feat-001 (not a separate
  feature like auth-service), i18n retrofit going beyond auth-service's residual.

## Blockers / Risks

- None blocking. See `progress.md` "Bloqueios / Riscos" for the 2 accepted residuals (TOCTOU on
  the admin route, `TenantSchemaFilter` passing through without `X-Tenant-Id` enforcement) and
  the unbounded-but-low-cardinality `migratedSchemas` cache — all analyzed and accepted, not
  defects.

## Next Session Startup

1. Read `../../CLAUDE.md` and `../../docs/services/bets-service.md`.
2. Read this directory's `CLAUDE.md`, `feature_list.json`, `progress.md`.
3. Run `./init.sh` (needs Docker running for Testcontainers).

## Recommended Next Step

- Start `feat-002` (Catálogos base — `SPORT`/`LEAGUE`/`MARKET`/`TIPSTER`). Plan Reviewer first.
  First feature to introduce real JPA entities, and therefore Hibernate multi-tenancy
  (`CurrentTenantIdentifierResolver`/`MultiTenantConnectionProvider`) — reuse the mechanism
  already documented in `docs/CONVENTIONS.md` from `auth-service feat-002.5`, but re-confirm the
  exact configuration keys via `javap` against the installed jar before assuming from memory.
