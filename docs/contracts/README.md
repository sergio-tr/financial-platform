# Contracts Registry (R35)

Contracts are materialized by owning MWUs under repository root:
- `contracts/asyncapi/**` — AsyncAPI channels/messages
- `contracts/schemas/**` — JSON Schemas referenced by messages

Accepted packs: SER-92..SER-95 (`ACCEPTED_FOR_P0_FREEZE`, 2026-10-09). Canonical transport artifacts live under repository-root `contracts/**`; this file is guidance only.

Q04 domain slices materialize incrementally. SER-92 slice (2026-11-17): identity/workspace/configuration/audit enums, problem codes and UUID id pattern. SER-93 connectivity slice (2026-11-18): connectivity canonical enums only. SER-93 instrument slice (2026-11-18): instrument §4.1 identity enums only. SER-93 market slice (2026-11-18): market §5.1 quote/adjustment enums only. SER-93 macro slice: R5-01 structural authority audit recorded under `docs/planning/phase0-closure/SER-93_MACRO_STRUCTURAL_AUTHORITY_AUDIT_R5.md` (gap register; no `contracts/schemas/macro/**` yet). SER-93 research slice (2026-11-18): research §7.2 news content availability enum only — no AsyncAPI routes, command payloads, §8 problem-code registry or handler surfaces yet. SER-93 R7-01 (2026-11-18): integral coverage audit matrix in `docs/planning/phase0-closure/SER-93_COVERAGE_AUDIT_R7.md` (non-normative; no new schemas). SER-93 R8-01 (2026-11-20): shared source-quality authority audit in `docs/planning/phase0-closure/SER-93_SHARED_SOURCE_QUALITY_AUTHORITY_AUDIT_R8.md` (§1/§B consumer and dependency matrix; R8-02 materialization blocked pending human ratification; no new schemas). SER-93 R8-03-01 (2026-11-20): pack-level §8 stable problem-code registry in `contracts/schemas/ser-93/problem-codes.v1.schema.json` (33 codes; namespace split from SER-92 §7; no view/command/AsyncAPI slices). SER-93 R8-03-02 (2026-11-20): pack-level §9 initial safe domain-event code registry in `contracts/schemas/ser-93/domain-event-codes.v1.schema.json` (17 code strings; no payload/envelope/AsyncAPI).

