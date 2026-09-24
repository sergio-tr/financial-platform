# ADR-0021: Transactional outbox

Status: **ACCEPTED**  
Date: **2026-09-23**

## Context

This decision is part of Architecture Baseline v1.0 for the local financial platform.

## Decision

Use a transactional outbox for durable application events that must outlive the committing transaction.

## Rationale

Avoids DB/event dual-write inconsistencies and decouples workflow/domain state from WebSocket availability.

## Consequences and constraints

Implementations must conform to this decision and must not hide an incompatible design behind an adapter or temporary shortcut. Tests and architecture checks should enforce the decision where practical.

## Revisit trigger

Realtime publisher consumes outbox events.

## Supersession

Any incompatible change requires a new ADR that explicitly supersedes this record.
