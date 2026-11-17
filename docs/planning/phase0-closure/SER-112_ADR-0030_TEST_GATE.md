# SER-112 — ADR-0030 test gate

Status: **ACCEPTED** (carve-out promoted 2026-11-17). Owner: SER-112.  
`IMPLEMENTATION_LOCK=ON`.

## Rule

Normative tests and CI selectors may assume the ADR-0030 RabbitMQ carve-out is **ACCEPTED** via **ADR-0037** and updated **D-030** in `decision-register.md`.

## Allowed

- Documenting and running T3 integration suites (RabbitMQ + WebSocket) mapped to SER-38 and REQ-REALTIME-002.
- Local architecture text in `websocket-architecture.md` describing relay destinations.
- Architecture checks that require relay-only RabbitMQ in T3 lanes per **ADR-0037**.

## Forbidden

- Using RabbitMQ for general event sourcing, outbox delivery, or secret-bearing payloads (R43/R46 sink policy).
- Additional message buses or SaaS brokers without a future ADR.
- Treating ADR-0030 as forbidding mandated broker-relay suites.

## Related

- Investigation: `SER-20_ADR-0030_STOMP_BROKER_SCOPE.md`
- Accepted ADR: `docs/adr/ADR-0037-stomp-broker-relay-carve-out.md`
- Promotion source: `ADR-0030-STOMP-BROKER-RELAY-AMENDMENT-PROPOSED.md`