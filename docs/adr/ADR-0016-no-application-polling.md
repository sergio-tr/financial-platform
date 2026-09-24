# ADR-0016: No application polling

Status: **ACCEPTED**  
Date: **2026-09-23**

## Context

This decision is part of Architecture Baseline v1.0 for the local financial platform.

## Decision

No frontend/backend component polls another application component for state. WebSocket replay/events are used internally; scheduled external pulls are allowed; Temporal worker pollers and heartbeats are infrastructure details.

## Rationale

Prevents inefficient/ambiguous realtime architecture while preserving provider sync feasibility.

## Consequences and constraints

Implementations must conform to this decision and must not hide an incompatible design behind an adapter or temporary shortcut. Tests and architecture checks should enforce the decision where practical.

## Revisit trigger

Reconnect backoff is transport recovery, not business polling.

## Supersession

Any incompatible change requires a new ADR that explicitly supersedes this record.
