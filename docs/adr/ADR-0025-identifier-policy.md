# ADR-0025: Identifier policy

Status: **ACCEPTED**  
Date: **2026-09-23**

## Context

This decision is part of Architecture Baseline v1.0 for the local financial platform.

## Decision

Use UUIDv7 for internal aggregate/entity identifiers; treat provider IDs as opaque external identifiers.

## Rationale

Stable internal identity must not depend on ticker, ISIN or provider conventions.

## Consequences and constraints

Implementations must conform to this decision and must not hide an incompatible design behind an adapter or temporary shortcut. Tests and architecture checks should enforce the decision where practical.

## Revisit trigger

Natural identifiers remain searchable attributes.

## Supersession

Any incompatible change requires a new ADR that explicitly supersedes this record.
