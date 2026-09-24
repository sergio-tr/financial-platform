# ADR-0014: Charting stack

Status: **ACCEPTED**  
Date: **2026-09-23**

## Context

This decision is part of Architecture Baseline v1.0 for the local financial platform.

## Decision

Use Apache ECharts for general analytical visualizations and Lightweight Charts for financial time-series initially.

## Rationale

Combines broad visualization coverage with high-quality financial charts.

## Consequences and constraints

Implementations must conform to this decision and must not hide an incompatible design behind an adapter or temporary shortcut. Tests and architecture checks should enforce the decision where practical.

## Revisit trigger

Feature code should use adapters to reduce vendor lock-in.

## Supersession

Any incompatible change requires a new ADR that explicitly supersedes this record.
