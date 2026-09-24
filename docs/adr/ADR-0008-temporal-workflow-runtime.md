# ADR-0008: Temporal workflow runtime

Status: **ACCEPTED**  
Date: **2026-09-23**

## Context

This decision is part of Architecture Baseline v1.0 for the local financial platform.

## Decision

Use Temporal self-hosted behind `WorkflowEnginePort`.

## Rationale

Durable retries, schedules, cancellation, recovery and workflow state are core requirements and expensive to implement correctly in-house.

## Consequences and constraints

Implementations must conform to this decision and must not hide an incompatible design behind an adapter or temporary shortcut. Tests and architecture checks should enforce the decision where practical.

## Revisit trigger

Temporal SDK annotations/types remain infrastructure-only.

## Supersession

Any incompatible change requires a new ADR that explicitly supersedes this record.
