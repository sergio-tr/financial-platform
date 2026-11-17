# Governance validators

## Traceability (SER-97)

- `validate-traceability.py --profile foundation` — SER-96 schema, relation matrix, duplicate/orphan/superseded checks, Phase-0 invariants (default CI profile).
- `validate-traceability.py --profile full` — graph coverage rules (requires SER-102 mappings; not CI-default until SER-102 completes).
- `validate-pr-diff-impact.py` — Stage B PR diff classifier; blocks product/migration paths while `IMPLEMENTATION_LOCK=ON`. Accepted-pack JSON Schema materialization under `contracts/schemas/**` with governance paths is permitted (SER-20 Q04 D1). `contracts/asyncapi/**` remains blocked until an owning MWU widens the carve-out.
- `generate-trace-matrix.py` / `--check-clean` — deterministic human-readable matrix from the graph.
- `validate-pr-trace.py` — branch / PR Linear primary / headings / contribution attribution (Git-local).
- `run-traceability-fixtures.py` — synthetic positive/negative fixtures.
- Modules live under `scripts/governance/traceability/`.

## Other

- `validate-task-packet.py` rejects invalid execution packets before edits.
- `validate-ser-101-requirements.py` asserts SER-101 requirement body materialization invariants (73/27/9 aliases/paths).

CI installs pinned PyYAML/jsonschema and runs the foundation set.
