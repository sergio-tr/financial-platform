# Contracts Registry (R35)

Contracts are materialized by owning MWUs under repository root:
- `contracts/asyncapi/**` — AsyncAPI channels/messages
- `contracts/schemas/**` — JSON Schemas referenced by messages

Accepted packs: SER-92..SER-95 (`ACCEPTED_FOR_P0_FREEZE`, 2026-10-09). Canonical transport artifacts live under repository-root `contracts/**`; this file is guidance only.

Q04 domain slices materialize incrementally. SER-92 slice (2026-11-17): identity/workspace/configuration/audit enums, problem codes and UUID id pattern. SER-93 connectivity slice (2026-11-18): connectivity canonical enums only. SER-93 instrument slice (2026-11-18): instrument §4.1 identity enums only — no AsyncAPI routes, command payloads, §8 problem-code registry or handler surfaces yet.

