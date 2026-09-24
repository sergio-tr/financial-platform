# WebSocket Application Architecture

## Transport boundary

- HTTPS: static Angular assets, authentication/session bootstrap, WebSocket upgrade, local health endpoints.
- WSS: all functional application commands, queries, responses, domain/application events and AI/report streams.
- No SSE.
- No application-level periodic polling.
- No SockJS fallback.

## Protocol

STOMP 1.2 over native secure WebSocket. Application semantics are defined above STOMP; STOMP destinations are transport routing, not the domain contract.

Client -> server destinations:
- `/app/v1/commands/**`
- `/app/v1/queries/**`

Server -> authenticated user:
- `/user/queue/v1/replies`
- `/user/queue/v1/events`
- `/user/queue/v1/streams`
- `/user/queue/v1/errors`

Financial/private payloads must not be broadcast on global public topics.

## Canonical metadata

Every message includes:
- `messageId` UUIDv7;
- `correlationId`;
- optional `causationId`;
- `traceId`;
- `protocolVersion`;
- `schema` identifier/version;
- `timestamp` UTC;
- `workspaceId` where applicable.

Commands additionally carry an idempotency key when a repeated delivery could cause side effects.

## Message semantics

Allowed application categories:
- COMMAND
- QUERY
- RESPONSE
- EVENT
- ERROR
- STREAM_START
- STREAM_CHUNK
- STREAM_END

## Reconnection and replay

Realtime events that matter to client consistency receive monotonically ordered stream sequence numbers. The client persists only the last non-sensitive sequence cursor required for the current session. On reconnect it requests replay from its last acknowledged sequence over WSS, then returns to live delivery.

Reconnect uses exponential backoff with jitter, capped at 30 seconds. STOMP heartbeats are transport liveness checks only; they must not fetch business state.

## Backpressure policy

Application stream classes declare one of:
- `LOSSLESS`: transactions, completed workflow results, audit-critical events;
- `COALESCE`: repeated progress/quote updates where only the newest state matters;
- `DROP_OLD`: non-authoritative transient visualization updates.

Bounded queues are mandatory. Overflow of a LOSSLESS stream is an operational error, not silent data loss.

## Binary/report streaming

Large report/export payloads may be transferred as chunked streams containing `streamId`, MIME type, expected length, SHA-256, chunk index and total chunks. Client verifies checksum before exposing the result.

## Contract-first rule

No new `@MessageMapping` route is implemented until its AsyncAPI channel/message and JSON Schema payload exist or are updated first.
