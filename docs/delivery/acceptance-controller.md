# Acceptance Controller Contract

Owners: SER-113 / P0-R50 / P0-R58  
Status: CANONICAL for isolated acceptance environment and human approval  
Amended by: P0-R59 / SER-132 (autoexecution clauses superseded)

Acceptance target is the dedicated Ubuntu 24.04 Hyper-V/Multipass VM `sergiotr-acceptance`.

## Frozen isolation

- WSL = DEVELOPMENT only.
- acceptance VM = isolated candidate runtime.
- candidate identity = unprivileged `sergiotr`.
- rootless Docker only.
- host filesystem mounts forbidden.
- production-local DB, SecretVault and backup paths forbidden.
- acceptance lanes use synthetic data only.

## Candidate state

A candidate record contains primary SER id, PR number, exact head SHA, artifact digests, fixture catalog/version, automated result, manual decision and cleanup result. Any head-SHA change invalidates the record.

## Controller verbs

`verify`, `deploy`, `test`, `status`, `accept`, `reject`, `destroy`.

Lifecycle-changing verbs require the trusted controller. Candidate code cannot invoke privileged broker functions.

## Manual package

When verification gates pass, present Sergio the PR/SER, exact SHA, stable LAN preview URL when provisioned, automated evidence, device/browser checklist and ACCEPT/REJECT controls bound to that SHA.

Human ACCEPT/REJECT is mandatory. No production promotion is implied by merge or acceptance.

## P0-R59 constraints

- Acceptance is human-controlled. It is not triggered by Slack/Linear dispatch, agent heartbeat, ORCHESTRATOR_TICK or automatic next-task selection.
- A JIT/self-hosted acceptance runner is not a Phase-0 prerequisite and is not required for SER-20 readiness.
- SER-111 is `CANCELED_SUPERSEDED` and does not own this contract.
- GitHub Actions remains CI/verification only.
