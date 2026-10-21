# Delivery Orchestration Contract

Owners: SER-111 / P0-R48

## Candidate lifecycle

PR opened/update -> cloud CI on exact head SHA -> artifact/evidence -> trusted controller verifies SHA+digest -> one-job JIT runner inside sergiotr-acceptance -> isolated synthetic deploy -> automated acceptance -> HUMAN ACCEPT/REJECT for exact SHA -> merge-ready status -> cleanup.

## Trust boundary

- PR-head code never controls privileged orchestration.
- Do not use pull_request_target to execute candidate code with secrets.
- Candidate execution has no broker/App private key, production credential, SecretVault key, backup credential or repository write credential.
- Candidate has no privileged/rootful Docker socket or host mount.
- Privileged status publication is performed by the trusted controller after reading candidate evidence.
- Stale SHA invalidates prior acceptance.

## GitHub / Linear / Slack

Primary SER-* is the lifecycle key. Slack is UX/evidence, not authority. Merge and post-merge audit must not auto-close an issue whose DoD is not proven.

## Runner

sergiotr-runner-broker is trusted and creates an ephemeral one-job runner in the already-proven sergiotr-acceptance VM. The runner state is removed after the candidate. Broker credentials never enter candidate execution.

Actual broker credentials/bootstrap are a local human/operations gate and are never stored in Git.
