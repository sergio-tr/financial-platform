# ADR-0009: MCP infrastructure boundary

Status: **ACCEPTED**  
Date: **2026-09-23**

## Context

This decision is part of Architecture Baseline v1.0 for the local financial platform.

## Decision

MCP types and clients are permitted only in infrastructure adapters.

## Rationale

MCP is a replaceable integration mechanism, not the financial domain model.

## Consequences and constraints

Implementations must conform to this decision and must not hide an incompatible design behind an adapter or temporary shortcut. Tests and architecture checks should enforce the decision where practical.

## Revisit trigger

Application/domain APIs remain stable if MCP changes/disappears.

## Supersession

Any incompatible change requires a new ADR that explicitly supersedes this record.
