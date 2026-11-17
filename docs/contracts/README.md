# Contracts Registry (R35)

Contracts are materialized by owning MWUs under repository root:
- `contracts/asyncapi/**` — AsyncAPI channels/messages
- `contracts/schemas/**` — JSON Schemas referenced by messages

Accepted packs: SER-92..SER-95 (`ACCEPTED_FOR_P0_FREEZE`, 2026-10-09). Canonical transport artifacts live under repository-root `contracts/**`; this file is guidance only.

Q04 domain slices materialize incrementally. First SER-92 slice (2026-11-17): shared enums, problem codes and UUID id pattern under `contracts/schemas/**` only — no AsyncAPI routes, command payloads or handler surfaces yet.

