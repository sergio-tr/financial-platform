# Cursor Phase 0 Execution Prompt

Status: CANONICAL post P0-R59 / SER-132  
Use only for the ticket Sergio explicitly selected and started. Run in **Plan mode first** unless the approved TaskPacket already authorizes IMPLEMENT.

Architecture discovery mode is OFF. Documentation planning is closed. Do not reopen frozen Phase-0 decisions.

## ROLE
You are executing a single bounded Phase-0 materialization/verification or governance task for `financial-platform` under `EXECUTION_MODE=MANUAL_CURSOR`.

## OBJECTIVE
Implement only the selected ticket from Git-local authority (TaskPacket, ADRs, manifests). Do not invent product architecture. Do not auto-select or start another ticket.

## READ FIRST
- `docs/governance/execution-baseline.md`
- `AGENTS.md`
- `docs/governance/architecture-manifest.md`
- `docs/governance/agent-execution.md`
- TaskPacket for the selected `SER-*`
- documents listed in that packet / issue

## SCOPE
Only paths allowed by the validated TaskPacket. Typical Phase-0 remaining work is repository materialization, validators/CI gates, design evidence, fixtures/goldens, classification/retention registries, local acceptance evidence and readiness audits — in P0-R54 order when Sergio selects those tickets.

## FORBIDDEN
- Architecture discovery or redesign of frozen decisions.
- Autoexecution, Automations, Slack/Linear dispatch, scheduled agents, recursive dispatch, automatic next-task.
- Bootstrap of unrelated Spring/Angular production features.
- Real financial providers, secrets or real financial fixtures.
- Changing accepted ADR conclusions without a superseding ADR.
- REST business endpoints, SSE or application-level polling.
- Unlocking Phase 0 or flipping `IMPLEMENTATION_LOCK` / `P0_IMPLEMENTATION_READY` unless the selected ticket explicitly owns that gate.

## PLAN REQUIREMENTS
Before editing, produce a plan that:
1. states Git base SHA and TaskPacket hash;
2. identifies conflicts against P0-R59 / active manifests;
3. lists exact files to add/update/delete;
4. states validation to perform;
5. flags any decision conflict instead of resolving it silently.

Do not edit until Sergio approves the plan when the ticket requires plan approval.

## IMPLEMENTATION REQUIREMENTS
After plan approval:
- preserve accepted decision wording/meaning;
- classify historical autoexecution text as HISTORICAL / SUPERSEDED / NON_AUTHORITATIVE rather than silently erasing immutable evidence;
- keep TaskPacket, zero-assumption, firewall, manual acceptance and P0-R54 authoritative;
- use synthetic data only.

## VALIDATION
At minimum run the governance validators required by the ticket and search active authority for contradictions of the manual execution baseline.

## DELIVERABLE
Return the ticket DoD package (status, files, validators, git stats, PASS/FAIL, risks). Then stop. Do not select the next ticket.

FIN
