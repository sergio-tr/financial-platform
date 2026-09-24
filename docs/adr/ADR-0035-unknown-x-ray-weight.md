# ADR-0035: Unknown X-Ray weight

Status: **ACCEPTED**  
Date: **2026-09-23**

## Context

This decision is part of Architecture Baseline v1.0 for the local financial platform.

## Decision

X-Ray preserves unknown composition weight and never redistributes it as known exposure.

## Rationale

Avoids misleading exposure conclusions from partial fund composition.

## Consequences and constraints

Implementations must conform to this decision and must not hide an incompatible design behind an adapter or temporary shortcut. Tests and architecture checks should enforce the decision where practical.

## Revisit trigger

Coverage is displayed with X-Ray outputs.

## Supersession

Any incompatible change requires a new ADR that explicitly supersedes this record.
