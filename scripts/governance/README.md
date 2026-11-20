# Governance validators

## Traceability (SER-97)

- `validate-traceability.py --profile foundation` — SER-96 schema, relation matrix, duplicate/orphan/superseded checks, Phase-0 invariants (default CI profile).
- `validate-traceability.py --profile full` — graph coverage rules (requires SER-102 mappings; not CI-default until SER-102 completes).
- `validate-pr-diff-impact.py` — Stage B PR diff classifier; blocks product/migration paths while `IMPLEMENTATION_LOCK=ON`. Accepted-pack JSON Schema materialization under `contracts/schemas/**` with governance paths is permitted (SER-20 Q04 D1). `contracts/asyncapi/**` remains blocked until an owning MWU widens the carve-out. SER-140 V0 engineering substrate paths (`backend/application/**`, `frontend/app/**`, bootstrap tooling) are permitted when the diff is exclusively under the SER-140 carve-out plus governance paths.
- `generate-trace-matrix.py` / `--check-clean` — deterministic human-readable matrix from the graph.
- `validate-pr-trace.py` — branch / PR Linear primary / headings / contribution attribution (Git-local).
- `validate-pr-body-preclaim.py` — R9-02 draft PR body check before opening a PR (`CURSOR_AGENT_PR_BODY_*` envelope, no `SER-NNN`, SER-96 sections).
- `run-traceability-fixtures.py` — synthetic positive/negative fixtures.
- Modules live under `scripts/governance/traceability/`.

## Other

- `validate-task-packet.py` rejects invalid execution packets before edits.
- `validate-ser-101-requirements.py` asserts SER-101 requirement body materialization invariants (73/27/9 aliases/paths).
- `validate-ser-93-problem-codes.py` asserts SER-93 pack §8 problem-code registry fidelity (`contracts/schemas/ser-93/problem-codes.v1.schema.json`).
- `validate-ser-93-domain-event-codes.py` asserts SER-93 pack §9 domain-event code registry fidelity (`contracts/schemas/ser-93/domain-event-codes.v1.schema.json`).

CI installs pinned PyYAML/jsonschema and runs the foundation set.
