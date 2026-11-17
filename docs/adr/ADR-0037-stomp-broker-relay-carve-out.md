# ADR-0037: STOMP broker relay carve-out (partial ADR-0030 supersession)

Status: **ACCEPTED**  
Date: **2026-11-17**  
Supersedes: **ADR-0030** (partial — RabbitMQ STOMP relay only)

## Context

ADR-0030 (2026-09-23) excludes Kafka, RabbitMQ, Redis, and Kubernetes in the initial platform. SER-38 (Done), REQ-REALTIME-002, P0-R42, and `docs/architecture/websocket-architecture.md` mandate a private-network RabbitMQ deployment used exclusively as the STOMP broker relay, with PostgreSQL as durable replay authority.

Authority ratification: FP-P0-SEQUENTIAL-02 Q02 (2026-11-17). Prior proposal: `docs/planning/phase0-closure/ADR-0030-STOMP-BROKER-RELAY-AMENDMENT-PROPOSED.md`.

## Decision

1. **In scope (required when the realtime stack ships):** one self-hosted RabbitMQ instance on the LAN used **exclusively** as the STOMP broker relay defined in SER-38, P0-R42, and `docs/architecture/websocket-architecture.md` (exactly the three `amq.direct/sergiotr.v1.*` user destinations). RabbitMQ holds **no** durable financial truth; PostgreSQL remains authoritative for replay and resync.
2. **Out of scope (unchanged from ADR-0030):** Kafka; Redis as platform infrastructure; Kubernetes; additional message buses or SaaS brokers without a future ADR; using RabbitMQ for general event sourcing, outbox delivery, or secret-bearing payloads (forbidden by R43/R46 sink policy).
3. **Operations:** per P0-R30 R30-10, RabbitMQ is **not** restored from backup; treat as ephemeral relay with PostgreSQL-backed recovery.

## Consequences

- Architecture and module tests may require RabbitMQ **only** in the relay role and integration lanes mapped to SER-38 and SER-112 T3.
- CI governance must not treat ADR-0030 as forbidding mandated broker-relay suites; ADR-0037 is the register-level carve-out for that relay role only.
- ADR-0030 remains **ACCEPTED** for Kafka, Redis, Kubernetes, and non-relay RabbitMQ uses.

## References

- SER-38, REQ-REALTIME-001, REQ-REALTIME-002, P0-R30 R30-10, P0-R42
- `docs/planning/phase0-closure/SER-20_ADR-0030_STOMP_BROKER_SCOPE.md`
- `docs/planning/phase0-closure/SER-112_ADR-0030_TEST_GATE.md`