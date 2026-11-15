# Execution baseline — P0-R59 / SER-132

Status: CANONICAL operational authority  
Owners: SER-132 / P0-R59  
Supersedes: autoexecution-specific clauses of P0-R48 / P0-R50 / P0-R52 / P0-R53 / P0-R56 and SER-111

## Operational state

```text
P0_PLANNING_FROZEN=YES
DOCUMENTATION_PLANNING_CLOSED=YES
ARCHITECTURE_DISCOVERY_MODE=OFF

EXECUTION_MODE=MANUAL_CURSOR
EXECUTION_VENUES=LOCAL,CURSOR_CLOUD

HUMAN_TASK_SELECTION=REQUIRED
HUMAN_TASK_START=REQUIRED

AUTOEXECUTION=OFF
AUTOMATIONS=OFF
AUTO_TASK_SELECTION=OFF
AUTO_NEXT_TASK=OFF
AUTO_MERGE=OFF

IMPLEMENTATION_LOCK=ON
P0_IMPLEMENTATION_READY=NO

SER-111=CANCELED_SUPERSEDED
DELEGATED_ORCHESTRATION=ON   # SER-136 (scope-limited; see below)
```

## Manual Cursor rule

Sergio explicitly selects every ticket and explicitly starts every execution, EXCEPT under the SER-136 delegated-orchestration handshake when an active Delegation Grant is in force. In that case ChatGPT may select the next executable ticket from the current dependency graph and set only that ticket In Progress.

Cursor may run in:

- Cursor local;
- Cursor Cloud started manually by Sergio.

Cursor Cloud is only another execution venue. It is not an auto-orchestration mechanism.

## Forbidden autoexecution paths

Do not use for this project:

- Slack/Linear as dispatchers — EXCEPT the SER-136 delegated-orchestration handshake limited to PLAN/IMPLEMENT/REVIEW handoffs and status publication; Slack/Linear remain non-authoritative for product edits;
- `@cursor` as automatic trigger;
- scheduled agents;
- Cursor Automations;
- cron execution;
- webhook autoexecution;
- automatic issue selection;
- recursive agent dispatch;
- automatic next-task execution;
- `ORCHESTRATOR_TICK`;
- automatic `CURSOR_HANDOFF`;
- autonomous Cursor runner;
- JIT/self-hosted acceptance runner as a Phase-0 prerequisite;
- heartbeat-triggered execution.

## Delegated orchestration (SER-136)

Scope-limited delegated orchestration is permitted only with an active Delegation Grant explicitly given by Sergio and revocable at any time. Under this grant:

- ChatGPT may select the next executable ticket strictly from the current dependency graph and set only that ticket In Progress;
- ChatGPT may issue PLAN/IMPLEMENT/REVIEW handoffs and status updates in `#financial-platform` without a fresh user message when authority and DoD are unambiguous;
- zero-assumption and fail-closed remain mandatory; ambiguity or missing authority blocks progress;
- a single Cursor work unit may be active at any time; no cross-ticket widening;
- this delegation does not authorize product edits beyond existing locks: AUTOEXECUTION=OFF; IMPLEMENTATION_LOCK=ON; P0_IMPLEMENTATION_READY=NO; no direct `main` push, no force-push/history rewrite; squash-only after checks/review/DoD; no production/secrets/real financial data/trading.

## Allowed Cloud Agent actions (manual venue only)

A Cloud Agent may, for the ticket Sergio selected and started:

- work on that ticket only;
- modify only its branch;
- run tests;
- generate evidence;
- commit/push;
- prepare a Draft PR when Sergio requests it.

A Cloud Agent must never:

- choose the next ticket;
- merge to `main`;
- start another task;
- unlock Phase 0;
- modify frozen decisions;
- receive real credentials, real financial data, exports, statements, private snapshots, production SecretVault, broker/provider credentials or private backups.

Use synthetic data only.

## What remains authoritative

- TaskPacket v1 and zero-assumption rules (P0-R52 / SER-115);
- Agent Execution Firewall;
- Definition of Ready / Definition of Done;
- branch and PR discipline;
- manual acceptance and human approval;
- P0-R54 dependency ordering;
- traceability;
- P0-R48 / P0-R50 / P0-R56 for PR state, CI evidence, acceptance invalidation and explicit human approval — excluding superseded autoexecution clauses.

## GitHub Actions

GitHub Actions is CI/verification only. It is not an agent orchestrator.

## SER-111

SER-111 is `CANCELED_SUPERSEDED`. It does not block SER-20 or implementation readiness. A self-hosted/JIT acceptance runner may return only by an explicit new decision.
