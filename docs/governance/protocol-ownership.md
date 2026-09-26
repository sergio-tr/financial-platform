# Cross-System Protocol Ownership (R26)

Status: CANONICAL — defines versioning/compatibility and ownership for cross-system protocols.

## Scope
- Applies to messaging contracts (AsyncAPI channels/messages), binary artifact streams, and replay cursors.
- Contracts are canonicalized in Git under `contracts/**` when owned MWUs materialize them. Until then, references are PENDING.

## Versioning and compatibility
- Semantic compatibility: MAJOR breaks; MINOR additive backward-compatible; PATCH non-contract changes (docs/examples).
- Each message payload references a JSON Schema by `{schemaId}@{version}` and includes a `protocolVersion`.
- A manifest/hash of the effective contract must be tracked in PRs that change contracts. Breaking changes require MAJOR bump.

## Ownership and process
- Public APIs are exposed only via `<context>.api`. Outbound ports are consumer-owned.
- No Spring/JPA/Temporal/MCP/provider SDK types in public contracts (transport DTOs are distinct from domain/entities).
- Contract-first rule: handlers/routes are implemented only after the contract exists and is reviewed.
- Diff process: PR includes contract diff, compatibility assessment, and impacted UseCaseIds and events.

## Transport notes (R42)
- STOMP 1.2 only over native WSS. No SSE, no SockJS, no permessage-deflate.
- Client -> server: `/app/v1/commands/**`, `/app/v1/queries/**`
- Server -> user: `/user/exchange/amq.direct/gafi.v1.control|events|artifacts`
- Rabbit is an ephemeral relay; PostgreSQL is canonical for durable replay.

References: ADR-0003, ADR-0004, ADR-0005; R42 realtime additions.

