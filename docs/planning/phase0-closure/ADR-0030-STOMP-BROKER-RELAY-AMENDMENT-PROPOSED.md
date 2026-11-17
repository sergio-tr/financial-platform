# Proposed amendment: ADR-0030 STOMP broker relay carve-out

Status: **ACCEPTED — promoted** to `docs/adr/ADR-0037-stomp-broker-relay-carve-out.md` (2026-11-17, FP-P0-SEQUENTIAL-02 Q02).  
Routing: SER-20 D3 / SER-112 / SER-38.  
`IMPLEMENTATION_LOCK=ON`.

## Summary

When accepted, this record would **partially supersede** the RabbitMQ clause of **ADR-0030** / **D-030** to align with **SER-38** (Done), **REQ-REALTIME-002**, **P0-R42**, and `docs/architecture/websocket-architecture.md`.

## Problem

ADR-0030 (2026-09-23) states: do not introduce Kafka, **RabbitMQ**, Redis, or Kubernetes initially.

Later acceptance (**SER-38**, **REQ-REALTIME-002**) mandates a private-network **RabbitMQ** deployment as the **STOMP broker relay** only, with PostgreSQL as durable replay authority. The decision register still lists D-030 without an explicit carve-out.

## Proposed decision (if accepted)

1. **In scope (required when realtime stack ships):** one self-hosted RabbitMQ instance on the LAN used **exclusively** as the STOMP broker relay defined in SER-38 / P0-R42 and `websocket-architecture.md` (e.g. `amq.direct/sergiotr.v1.*` destinations). RabbitMQ holds **no** durable financial truth; PostgreSQL remains authoritative for replay and resync.
2. **Out of scope (unchanged from ADR-0030):** Kafka; Redis as platform infrastructure; Kubernetes; additional message buses or SaaS brokers without a future ADR; using RabbitMQ for general event sourcing, outbox delivery, or secret-bearing payloads (forbidden by R43/R46 sink policy).
3. **Operations:** per P0-R30 R30-10, RabbitMQ is **not** restored from backup; treat as ephemeral relay with PostgreSQL-backed recovery.

## Consequences (upon acceptance only)

- Architecture and module tests may require RabbitMQ **only** in the relay role and integration lanes mapped to SER-38 / SER-112 T3.
- CI governance must not treat ADR-0030 as forbidding mandated broker-relay suites once this amendment is **ACCEPTED**.
- Until acceptance, implementation planning continues under **SER-38 + REQ-REALTIME-002** with register-level ambiguity noted in `SER-20_ADR-0030_STOMP_BROKER_SCOPE.md`.

## Supersession mechanics (human gate)

Promotion checklist (not executed in Phase-0 doc MWUs):

1. Human reviewer accepts this proposal or equivalent text.
2. Either update ADR-0030 consequences with explicit carve-out **or** add a new **ACCEPTED** ADR that lists `Supersedes: ADR-0030 (partial — RabbitMQ STOMP relay only)`.
3. Update `decision-register.md` D-030 wording to match accepted text.
4. Re-run architecture-manifest and traceability review.

## Evidence

| Artifact | Role |
|----------|------|
| `SER-20_ADR-0030_STOMP_BROKER_SCOPE.md` | Investigation and chronology |
| `SER-112_ADR-0030_TEST_GATE.md` | Test/CI fail-closed until acceptance |
| ADR-0030, ADR-0003, ADR-0004 | Unchanged until promotion |

## References

- SER-38, REQ-REALTIME-001, REQ-REALTIME-002, P0-R30 R30-10, P0-R42