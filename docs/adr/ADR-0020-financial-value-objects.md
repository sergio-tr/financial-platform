# ADR-0020: Financial value objects

Status: **ACCEPTED**  
Date: **2026-09-23**

## Context

This decision is part of Architecture Baseline v1.0 for the local financial platform.

## Decision

Use BigDecimal-backed explicit value objects for Money, Price, Quantity, Percentage, FxRate, Weight and returns.

## Rationale

Floating point is inappropriate for authoritative financial calculations.

## Consequences and constraints

Implementations must conform to this decision and must not hide an incompatible design behind an adapter or temporary shortcut. Tests and architecture checks should enforce the decision where practical.

## Revisit trigger

Rounding is specified at boundaries, not arbitrarily during calculations.

## Supersession

Any incompatible change requires a new ADR that explicitly supersedes this record.
