# ADR-0029: Sensitive PWA caching

Status: **ACCEPTED**  
Date: **2026-09-23**

## Context

This decision is part of Architecture Baseline v1.0 for the local financial platform.

## Decision

Allow PWA shell/static caching but do not persist sensitive financial state for offline use by default.

## Rationale

Prevents mobile/browser cache becoming a shadow financial datastore.

## Consequences and constraints

Implementations must conform to this decision and must not hide an incompatible design behind an adapter or temporary shortcut. Tests and architecture checks should enforce the decision where practical.

## Revisit trigger

Disconnected UI must clearly disclose stale/offline state.

## Supersession

Any incompatible change requires a new ADR that explicitly supersedes this record.
