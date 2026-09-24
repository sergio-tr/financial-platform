# ADR-0015: No financial execution

Status: **ACCEPTED**  
Date: **2026-09-23**

## Context

This decision is part of Architecture Baseline v1.0 for the local financial platform.

## Decision

AI, workflows and connectors exposed to the platform may not execute buy/sell/transfer/withdraw operations.

## Rationale

The platform supports analysis and human decision-making without automated movement of money.

## Consequences and constraints

Implementations must conform to this decision and must not hide an incompatible design behind an adapter or temporary shortcut. Tests and architecture checks should enforce the decision where practical.

## Revisit trigger

Any future execution capability would require a new explicit security/regulatory ADR.

## Supersession

Any incompatible change requires a new ADR that explicitly supersedes this record.
