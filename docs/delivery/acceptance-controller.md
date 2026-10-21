# Acceptance Controller Contract

Owners: SER-113 / P0-R50 / P0-R58

Acceptance target is the dedicated Ubuntu 24.04 Hyper-V/Multipass VM sergiotr-acceptance.

## Frozen isolation

- WSL = DEVELOPMENT only.
- acceptance VM = isolated candidate runtime.
- candidate identity = unprivileged sergiotr.
- rootless Docker only.
- host filesystem mounts forbidden.
- production-local DB, SecretVault and backup paths forbidden.
- automatic lane uses synthetic data only.

## Candidate state

A candidate record contains primary SER id, PR number, exact head SHA, artifact digests, fixture catalog/version, automated result, manual decision and cleanup result. Any head-SHA change invalidates the record.

## Controller verbs

verify, deploy, test, status, accept, reject, destroy.

Lifecycle-changing verbs require the trusted controller. Candidate code cannot invoke privileged broker functions.

## Manual package

When automated gates pass, present Sergio the PR/SER, exact SHA, stable LAN preview URL when provisioned, automated evidence, device/browser checklist and ACCEPT/REJECT controls bound to that SHA.

No production promotion is implied by merge or acceptance.
