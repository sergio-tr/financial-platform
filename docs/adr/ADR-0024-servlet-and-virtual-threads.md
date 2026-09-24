# ADR-0024: Servlet and virtual threads

Status: **ACCEPTED**  
Date: **2026-09-23**

## Context

This decision is part of Architecture Baseline v1.0 for the local financial platform.

## Decision

Use Spring MVC/WebSocket with Java virtual threads; do not make the financial domain reactive.

## Rationale

Blocking provider/database integrations map naturally to virtual threads and keep domain APIs simple.

## Consequences and constraints

Implementations must conform to this decision and must not hide an incompatible design behind an adapter or temporary shortcut. Tests and architecture checks should enforce the decision where practical.

## Revisit trigger

Reactive adapters may bridge internally without leaking reactive types.

## Supersession

Any incompatible change requires a new ADR that explicitly supersedes this record.
