# Session Handoff — bets-service

## Current Objective

- Goal: backlog complete. `feat-001` through `feat-014` all `done`.
- Current status: `develop` up to date, all merges green (CI + SonarCloud).
- Branch / commit: `develop` (merge of `feature/SV-317`, story SV-317, 4 subtasks).

## Completed This Session (2026-09-10)

- [x] `feat-014` (epic-013 at root) fully implemented across 4 subtasks (SV-318..321) — see
      `progress.md` and the `evidence` field of `feat-014` (and each subtask) in
      `feature_list.json` for full detail. 3 independent changes: `TENANT_SETTINGS` +
      admin-gated `GET`/`PATCH /api/v1/settings`, `BET.betType` free-text → enum `PRE`/`LIVE`,
      `GET /api/v1/bankroll/balance?at=` (consolidated balance, `America/Sao_Paulo` civil-day
      cutoff, not naive UTC).
- [x] Cross-service role model decided via `AskUserQuestion`: PASETO claim `role` +
      `X-User-Role` header injected by `api-gateway` (this service has no `USER` table). Real
      casing bug caught by the Plan Reviewer and fixed in `auth-service feat-013` before this
      service ever consumed the header.
- [x] `Delivery Reviewer` + `Test Suite Auditor` + `Persistence Auditor` run in parallel
      (isolated-context subagents) against the full `feature/SV-317` vs `develop` diff. Real
      findings fixed: missing `@Transactional` on `BankrollService.getBalance` (3 unsynchronized
      aggregate reads), missing indexes on `transaction.created_at`/`bet_result.settled_at`,
      missing tenant-isolation test for `tenant_settings`, missing HTTP-boundary tests for
      `CreateBetRequest.betType`. One CHANGELOG finding verified and rejected as false positive.

## Verification Evidence

| Check | Command | Result | Notes |
|---|---|---|---|
| Build/test | `./init.sh` | exit 0 | 155 tests, 0 failures, JaCoCo 80% gate green. |
| CI | GitHub Actions, PRs #56-59 | green | #59 (feature→develop) includes SonarCloud, all green. |

## Files Changed

- New: `BankrollController`/`BankrollBalanceResponse`/`BankrollService`/`BankrollBalance`/
  `BankrollUseCase`, `SettingsController`/`TenantSettings*`/`AdminRoleRequiredException`,
  `BetType`/`BetTypeAttributeConverter`, 3 new Flyway migrations (tenant_settings, betType enum
  migration, indexes on transaction/bet_result time columns).
- Modified: `BettingHouseRepository`/`TransactionRepository`/`BetResultRepository` (+ JPA
  adapters) gained new all-houses aggregate methods; `Bet`/`BetJpaEntity`/`CreateBetRequest`/
  `CreateBetCommand`/`BetResponse`/`BetCreatedPayload` (`String betType` → `BetType`);
  `DomainExceptionHandler` (new exception in the allowlist).
- Root `docs/services/bets-service.md`, `docs/API-CONTRACTS.md`,
  `docs/contracts/bet-created.schema.json`, `docs/DECISIONS-LOG.md` updated in the same logical
  commits.

## Decisions Made

- See `progress.md` "feat-014 fechada" section for the full rationale, including the accepted
  deviation (no migration-behavior test for dirty legacy `bet_type` rows — structural limitation
  of `TenantSchemaIntegrationSupport`, documented in `feat-014.2`'s `evidence` field).

## Blockers / Risks

- **`git stash` pending in this repo**: found on resuming this session, unrelated to `feat-014`
  — a `feat-015` ("CI: build e push da imagem Docker pro GHCR") had `plan_review` and subtasks
  already written into `feature_list.json`, plus a matching `.github/workflows/ci.yml` edit,
  neither ever committed. Left stashed (message "stray uncommitted feat-015...") rather than
  committed/discarded/finished, since acting on it wasn't part of this session's scope and
  committing it would have violated WIP-max-1 while `feat-014` was still `in-progress`. Run
  `git stash list` / `git stash show -p stash@{0}` before starting new work in this repo.

## Next Session Startup

1. Read `../../CLAUDE.md` and `../../docs/services/bets-service.md`.
2. Read this directory's `CLAUDE.md`, `feature_list.json`, `progress.md`.
3. Run `./init.sh` (needs Docker running for Testcontainers).
4. **Run `git stash list`** — see "Blockers / Risks" above before picking new work here.

## Recommended Next Step

- No feature pending in this service's own backlog. If picking `feat-015` back up (Docker image
  push to GHCR), re-read its `plan_review` from the stash, verify it's still accurate, and follow
  the normal Plan Reviewer → Jira story → branch flow from there — don't just pop and commit
  blind, the stash predates this session's audit-driven fixes.
- Otherwise, next root-level eligible epics are `epic-014` (`stats-service`) and
  `epic-016`/`epic-018` (`stats-service`) — this service has no dependency on those, no action
  needed here.
