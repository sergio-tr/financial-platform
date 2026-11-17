# SER-20 — ADR-0030 vs STOMP broker relay scope (D3 investigation)

Status: INVESTIGATION COMPLETE — **amendment PROPOSED** in `ADR-0030-STOMP-BROKER-RELAY-AMENDMENT-PROPOSED.md` (human gate pending).  
Owner: SER-20 authority recovery / FP-P0 Q4. `IMPLEMENTATION_LOCK=ON`.

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

For runtime and test planning, **SER-38 + REQ-REALTIME-002 + `websocket-architecture.md`** control broker-relay behavior. **ADR-0030** remains LOCKED as written in Git ADR text and register until superseded.

## Recommended enmienda (human gate)

Formal proposal: **`ADR-0030-STOMP-BROKER-RELAY-AMENDMENT-PROPOSED.md`**. Test fail-closed rules: **`SER-112_ADR-0030_TEST_GATE.md`**.

Until promotion, treat D-030 as **ambiguous at register level** but **fail-closed on implementation** using SER-38 + REQ-REALTIME-002 for topology planning (not register-level waiver).

## Test-layer contract (no invention)

SER-38 mandatory tests and SER-112 T3 integration taxonomy govern broker tests. ADR-0030 architecture checks must not forbid those suites once the enmienda is accepted; until then CI should cite SER-38 evidence, not infer an ADR-0030 waiver.