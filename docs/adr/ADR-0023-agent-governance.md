# ADR-0023: Agent governance

Status: **ACCEPTED**  
Date: **2026-09-23**

## Context

This decision is part of Architecture Baseline v1.0 for the local financial platform.

## Decision

Repository rules/ADRs/contracts govern Cursor and other coding agents; use cost-aware model escalation and bounded context.

## Rationale

Prevents agent drift and unnecessary model-credit spend.

## Consequences and constraints

Implementations must conform to this decision and must not hide an incompatible design behind an adapter or temporary shortcut. Tests and architecture checks should enforce the decision where practical.

## Revisit trigger

No agent may override a locked ADR silently.

## Supersession

Any incompatible change requires a new ADR that explicitly supersedes this record.
