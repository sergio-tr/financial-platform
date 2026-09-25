# Traceability Review Adjudication (R39)

Status: CANONICAL

Purpose: govern adjudication of the R37 trace graph (relationships among REQ/NFR/P0-R/ADR/SER/DES/TEST/RUNBOOK/DOC).

Process:
1. Identify candidate edges and nodes to add/change.
2. Validate authority (requirements, ADRs, planning packs, manifests).
3. Check for duplicates, superseded IDs, and dangling references.
4. Decide: ACCEPTED / REVISE / REJECT with rationale.
5. Update `traceability.yaml` and re-run validation.

This process does not invent missing requirements or contracts; it records relationships and locations only.

