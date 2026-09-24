# ADR-0004: WebSocket business transport

Status: **ACCEPTED**  
Date: **2026-09-23**

## Context

This decision is part of Architecture Baseline v1.0 for the local financial platform.

## Decision

All functional business traffic uses WSS. HTTPS is limited to static bootstrap, authentication/session bootstrap, WebSocket upgrade and health.

## Rationale

Creates one consistent realtime interaction model without SSE/polling split-brain APIs.

## Consequences and constraints

Implementations must conform to this decision and must not hide an incompatible design behind an adapter or temporary shortcut. Tests and architecture checks should enforce the decision where practical.

## Revisit trigger

File/report transfer remains allowed through chunked WebSocket streams unless a future ADR changes the boundary.

## Supersession

Any incompatible change requires a new ADR that explicitly supersedes this record.
