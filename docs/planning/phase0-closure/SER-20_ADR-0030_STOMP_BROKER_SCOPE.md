# SER-20 — ADR-0030 vs STOMP broker relay scope (D3 investigation)

Status: INVESTIGATION COMPLETE — **amendment ACCEPTED** as **ADR-0037** (2026-11-17, FP-P0-SEQUENTIAL-02 Q02).  
Owner: SER-20 authority recovery / SER-112. `IMPLEMENTATION_LOCK=ON`.

## Conflicting literals (sources)

| Source | Claim |
|--------|--------|
| ADR-0030 + `D-030` in `decision-register.md` | Do not introduce Kafka, **RabbitMQ**, Redis or Kubernetes **initially** |
| SER-38 (`Done`) | **RabbitMQ is the accepted STOMP broker relay**; PostgreSQL is replay authority |
| REQ-REALTIME-002 (`LOCKED`) | RabbitMQ is **live relay**, not durable truth |
| `docs/architecture/websocket-architecture.md` | Exactly three `amq.direct/sergiotr.v1.*` destinations; broker is live relay |
| P0-R30 R30-10 (Linear `LOCKED`) | RabbitMQ is **not** restored from backup; ephemeral relay |
| SER-112 (scope) | Integration suites include **RabbitMQ/WebSocket** |

## Chronology

1. ADR-0030 / D-030 (2026-09-23) — broad initial exclusion.
2. SER-38 / O-04 (2026-09-24, `Done` 2026-10-30) — mandates RabbitMQ for STOMP relay semantics and lists broker integration tests.
3. SER-70 Git sync — repository text and manifest already assume broker destinations and PostgreSQL replay.
4. `decision-register.md` still points D-030 at ADR-0030 **without** an explicit carve-out for the accepted relay.

## Controlling authority (functional behavior)

For runtime and test planning, **SER-38 + REQ-REALTIME-002 + `websocket-architecture.md`** control broker-relay behavior. **ADR-0037** provides the register-level partial supersession of **ADR-0030** for relay-only RabbitMQ; **D-030** is reconciled in `decision-register.md`.

## Recommended enmienda (human gate)

Accepted ADR: **`docs/adr/ADR-0037-stomp-broker-relay-carve-out.md`** (from **`ADR-0030-STOMP-BROKER-RELAY-AMENDMENT-PROPOSED.md`**). Test gate: **`SER-112_ADR-0030_TEST_GATE.md`** (post-acceptance).

## Test-layer contract (no invention)

SER-38 mandatory tests and SER-112 T3 integration taxonomy govern broker tests. ADR-0030 architecture checks must not forbid those suites once the enmienda is accepted; until then CI should cite SER-38 evidence, not infer an ADR-0030 waiver.