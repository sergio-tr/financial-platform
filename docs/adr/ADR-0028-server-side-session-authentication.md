# ADR-0028: Server-side session authentication

Status: **ACCEPTED**  
Date: **2026-09-23**

## Context

This decision is part of Architecture Baseline v1.0 for the local financial platform.

## Decision

Use server-side sessions and secure cookies; do not store bearer JWTs in browser storage.

## Rationale

Reduces token theft surface and integrates with authenticated WebSocket handshake.

## Consequences and constraints

Implementations must conform to this decision and must not hide an incompatible design behind an adapter or temporary shortcut. Tests and architecture checks should enforce the decision where practical.

## Revisit trigger

Passkeys/WebAuthn may later augment authentication.

## Supersession

Any incompatible change requires a new ADR that explicitly supersedes this record.
