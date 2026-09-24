# ADR-0003: STOMP over WebSocket

Status: **ACCEPTED**  
Date: **2026-09-23**

## Context

This decision is part of Architecture Baseline v1.0 for the local financial platform.

## Decision

Use STOMP 1.2 over native secure WebSocket; no SockJS fallback.

## Rationale

Mature Spring support plus explicit application-level messaging semantics fit Angular/Spring well.

## Consequences and constraints

Implementations must conform to this decision and must not hide an incompatible design behind an adapter or temporary shortcut. Tests and architecture checks should enforce the decision where practical.

## Revisit trigger

Revisit if a materially better browser/server protocol becomes stable and migration benefit exceeds contract cost.

## Supersession

Any incompatible change requires a new ADR that explicitly supersedes this record.
