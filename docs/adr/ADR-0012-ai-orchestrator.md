# ADR-0012: AI orchestrator

Status: **ACCEPTED**  
Date: **2026-09-23**

## Context

This decision is part of Architecture Baseline v1.0 for the local financial platform.

## Decision

Use one financial orchestrator with skills, tools, context/evidence/policy layers before considering multiple independent agents.

## Rationale

Reduces orchestration complexity, duplicated context and local-model cost.

## Consequences and constraints

Implementations must conform to this decision and must not hide an incompatible design behind an adapter or temporary shortcut. Tests and architecture checks should enforce the decision where practical.

## Revisit trigger

A skill may later become a separate agent behind the same boundaries.

## Supersession

Any incompatible change requires a new ADR that explicitly supersedes this record.
