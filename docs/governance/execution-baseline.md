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
DEV_SYNTHETIC_V1_GATE=SCOPED

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
- scheduled agents; — EXCEPT the SER-136 delegated hourly control‑loop tick (ChatGPT‑hosted, not Cursor Automations) that only re-evaluates the single selected ticket and, when blocked, may select the next executable Phase‑0 ticket via Slack handoffs;
- Cursor Automations;
- cron execution;
- webhook autoexecution;
- automatic issue selection;
- recursive agent dispatch;
- automatic next-task execution; — EXCEPT the SER-136 delegated selection of the single next executable Phase‑0 ticket from the dependency graph when the active ticket is genuinely blocked;
- `ORCHESTRATOR_TICK`; — EXCEPT the SER-136 delegated hourly control‑loop tick defined below; this tick never performs product edits and is not a Cursor Automation;
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

### Delegated hourly control‑loop tick

- A ChatGPT‑hosted hourly control‑loop tick may re‑evaluate the state of the currently selected ticket. If that ticket is genuinely blocked, the tick may select the single next executable Phase‑0 ticket from the dependency graph and issue PLAN/IMPLEMENT/REVIEW handoffs in `#financial-platform`.
- The tick is not a Cursor Automation and does not perform code edits, merges, acceptance, or production actions. It posts Slack handoffs/status only and remains subject to zero‑assumption and fail‑closed rules.
- Exactly one Cursor work unit remains active at any time across ticks.

### Clarification: AUTOEXECUTION=OFF

`AUTOEXECUTION=OFF` means no autonomous product implementation (code edits, merges, acceptance or production actions) without explicit human gates. Delegated orchestration under SER‑136 is coordination only and does not constitute product autoexecution.

### Solo maintainer: Cursor Approval Agent as code review (SER-136)

With an active Delegation Grant, when the repository has a single human maintainer and Sergio has accepted Cursor Approval Agent as PR reviewer, **Cursor Approval Agent** — an automated **bot** actor, explicitly not a human reviewer — may satisfy the **code review** gate for in-scope PRs that remain within the ticket's `allowedPaths` and locks (`IMPLEMENTATION_LOCK=ON`, `P0_IMPLEMENTATION_READY=NO`, `AUTO_MERGE=OFF`).

This does not replace explicit **human maintainer** approval to merge, human **ACCEPT/REJECT** on the exact SHA, sensitive promotion decisions, or CI/DoD/traceability and squash-only merge discipline. Evidence must identify bot review distinctly from human approval.

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

## DEV synthetic V1 gate (SER-143)

`DEV_SYNTHETIC_V1_GATE=SCOPED` authorizes **future** small DEV/TEST synthetic functional verticals only via an explicit TaskPacket plus a row in `docs/governance/dev-synthetic-v1-carve-out-registry.yaml`. It does **not** set `IMPLEMENTATION_LOCK=OFF`, `P0_IMPLEMENTATION_READY=YES`, or unlock SER-98 / SER-20 final gates. Canonical policy: `docs/governance/dev-synthetic-v1-gate.md`. V0 substrate (SER-140..142) remains a separate carve-out.

## SER-111

SER-111 is `CANCELED_SUPERSEDED`. It does not block SER-20 or implementation readiness. A self-hosted/JIT acceptance runner may return only by an explicit new decision.
