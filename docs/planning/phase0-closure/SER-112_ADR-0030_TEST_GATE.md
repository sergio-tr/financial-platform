# SER-112 — ADR-0030 test gate (fail-closed)

Status: **ACTIVE** for Phase-0 documentation cut. Owner: SER-112 (partial MWU under FP-P0 Q4).  
`IMPLEMENTATION_LOCK=ON`.

## Rule

Normative tests and CI selectors **must not** assume the ADR-0030 RabbitMQ carve-out is **ACCEPTED** until `ADR-0030-STOMP-BROKER-RELAY-AMENDMENT-PROPOSED.md` is promoted per its human gate.

## Allowed before acceptance

- Documenting planned T3 integration suites (RabbitMQ + WebSocket) in SER-112 artifacts **as PLANNED**, mapped to SER-38 and REQ-REALTIME-002.
- Local architecture text in `websocket-architecture.md` that already describes relay destinations.

## Forbidden before acceptance

- Changing `decision-register.md` D-030 or ADR-0030 status to imply RabbitMQ is allowed via register without supersession.
- Adding release-blocking CI that fails open by inventing an ADR-0030 waiver.
- Marking broker-relay tests **IMPLEMENTED** with PASS evidence tied solely to the PROPOSED amendment.

## After human acceptance

Reconcile test catalog / SER-112 matrix with the promoted ADR text; architecture checks may then require relay-only RabbitMQ in T3 lanes.

## Related

- Investigation: `SER-20_ADR-0030_STOMP_BROKER_SCOPE.md`
- Proposal: `ADR-0030-STOMP-BROKER-RELAY-AMENDMENT-PROPOSED.md`