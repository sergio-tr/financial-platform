# ADR-0034: Connector data quality metadata

Status: **ACCEPTED**  
Date: **2026-09-23**

## Context

This decision is part of Architecture Baseline v1.0 for the local financial platform.

## Decision

Every connector result reports provenance, freshness, completeness and warnings.

## Rationale

Downstream analytics/AI must distinguish partial/stale inputs from complete current data.

## Consequences and constraints

Implementations must conform to this decision and must not hide an incompatible design behind an adapter or temporary shortcut. Tests and architecture checks should enforce the decision where practical.

## Revisit trigger

Quality metadata is part of connector contracts.

## Supersession

Any incompatible change requires a new ADR that explicitly supersedes this record.
