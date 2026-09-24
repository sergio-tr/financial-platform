# ADR-0019: Secrets boundary

Status: **ACCEPTED**  
Date: **2026-09-23**

## Context

This decision is part of Architecture Baseline v1.0 for the local financial platform.

## Decision

Use `SecretVaultPort` and opaque `SecretRef`; encrypted secret material stays outside LLM context and source control.

## Rationale

Prevents credential propagation across domain/AI layers.

## Consequences and constraints

Implementations must conform to this decision and must not hide an incompatible design behind an adapter or temporary shortcut. Tests and architecture checks should enforce the decision where practical.

## Revisit trigger

Master key is stored separately from database and repository.

## Supersession

Any incompatible change requires a new ADR that explicitly supersedes this record.
