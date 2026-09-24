# ADR-0027: Immutable portfolio snapshots

Status: **ACCEPTED**  
Date: **2026-09-23**

## Context

This decision is part of Architecture Baseline v1.0 for the local financial platform.

## Decision

Persist portfolio snapshots as immutable historical records rather than updating prior snapshots.

## Rationale

Enables auditability, time travel, historical analytics and evidence reproduction.

## Consequences and constraints

Implementations must conform to this decision and must not hide an incompatible design behind an adapter or temporary shortcut. Tests and architecture checks should enforce the decision where practical.

## Revisit trigger

Corrections are represented by new normalized data/snapshots with provenance.

## Supersession

Any incompatible change requires a new ADR that explicitly supersedes this record.
