# Governance validators

## Traceability (SER-97 Stage A)

- `validate-traceability.py --profile foundation` — SER-96 schema, relation matrix, duplicate/orphan/superseded checks, Phase-0 invariants (default CI profile).
- `validate-traceability.py --profile full` — Stage B coverage (requires SER-102 mappings; not CI-default).
- `generate-trace-matrix.py` / `--check-clean` — deterministic human-readable matrix from the graph.
- `validate-pr-trace.py` — branch / PR Linear primary / headings / contribution attribution (Git-local).
- `run-traceability-fixtures.py` — synthetic positive/negative fixtures.
- Modules live under `scripts/governance/traceability/`.

## Other

- `validate-task-packet.py` rejects invalid execution packets before edits.
- `validate-ser-101-requirements.py` asserts SER-101 requirement body materialization invariants (73/27/9 aliases/paths).

CI installs pinned PyYAML/jsonschema and runs the foundation set.
