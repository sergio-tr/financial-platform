# ADR-0032: Angular state model

Status: **ACCEPTED**  
Date: **2026-09-23**

## Context

This decision is part of Architecture Baseline v1.0 for the local financial platform.

## Decision

Use Angular Signals + RxJS with feature facades/stores; do not adopt global NgRx by default.

## Rationale

Keeps state management proportional to a single local application while retaining reactive composition.

## Consequences and constraints

Implementations must conform to this decision and must not hide an incompatible design behind an adapter or temporary shortcut. Tests and architecture checks should enforce the decision where practical.

## Revisit trigger

NgRx requires a demonstrated cross-feature state need.

## Supersession

Any incompatible change requires a new ADR that explicitly supersedes this record.
