# ADR-0005: AsyncAPI contract first

Status: **ACCEPTED**  
Date: **2026-09-23**

## Context

This decision is part of Architecture Baseline v1.0 for the local financial platform.

## Decision

Use AsyncAPI 3.1 and JSON Schema as canonical frontend/backend messaging contracts.

## Rationale

The product is message-driven rather than REST-centric.

## Consequences and constraints

Implementations must conform to this decision and must not hide an incompatible design behind an adapter or temporary shortcut. Tests and architecture checks should enforce the decision where practical.

## Revisit trigger

No handler precedes its contract.

## Supersession

Any incompatible change requires a new ADR that explicitly supersedes this record.
