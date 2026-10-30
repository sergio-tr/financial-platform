# Delivery Orchestration Contract

Owners: P0-R48 / P0-R59 (SER-132)  
Status: CANONICAL for PR/CI/human-promotion after P0-R59 autoexecution supersession

SER-111 is `CANCELED_SUPERSEDED` and is not an active owner or readiness blocker.

## Candidate lifecycle (active)

```text
PR opened/update
-> GitHub Actions CI/verification on exact head SHA
-> CI evidence/artifacts
-> human review and explicit next steps
-> optional isolated synthetic acceptance under SER-113 / P0-R50 when manually initiated
-> HUMAN ACCEPT/REJECT for exact SHA
-> merge-ready status only after human approval
-> cleanup
```

There is no autonomous agent tick, Slack/Linear dispatch, auto-select next issue, automatic next-task execution, auto-merge, or Phase-0-required JIT/self-hosted acceptance runner.

## Trust boundary

- PR-head code never controls privileged orchestration.
- Do not use `pull_request_target` to execute candidate code with secrets.
- Candidate execution has no broker/App private key, production credential, SecretVault key, backup credential or repository write credential.
- Candidate has no privileged/rootful Docker socket or host mount.
- Privileged status publication, if any, is performed by a trusted controller after reading candidate evidence — never by autonomous agent selection.
- Stale SHA invalidates prior acceptance.

## GitHub / Linear / Slack

- Primary `SER-*` is the lifecycle key.
- Slack and Linear are coordination/indexing UX only. They are not an execution control plane and must not dispatch agents.
- GitHub Actions is CI/verification only. It is not an agent orchestrator.
- Merge and post-merge audit must not auto-close an issue whose DoD is not proven.
- `AUTO_MERGE=OFF`.

## Acceptance runner

A self-hosted/JIT acceptance runner is **not** a Phase-0 prerequisite. It may be introduced later only by an explicit new decision. Until then, acceptance remains human-controlled under SER-113 / P0-R50 / P0-R58 isolation rules and synthetic data only.

## Historical note

Earlier drafts that required SER-111, ORCHESTRATOR_TICK, Slack/Linear automatic dispatch, or a mandatory JIT acceptance runner for Phase-0 readiness are `HISTORICAL` / `SUPERSEDED` / `NON_AUTHORITATIVE` relative to P0-R59.
