# ADR-0007: Scheduled provider pulls

Status: **ACCEPTED**  
Date: **2026-09-23**

## Context

This decision is part of Architecture Baseline v1.0 for the local financial platform.

## Decision

Permit explicit scheduled provider synchronization workflows when upstream providers do not push updates.

## Rationale

Financial providers may not offer webhooks; scheduled pulls are necessary for automation.

## Consequences and constraints

Implementations must conform to this decision and must not hide an incompatible design behind an adapter or temporary shortcut. Tests and architecture checks should enforce the decision where practical.

## Revisit trigger

These are workflows, not UI/application state polling.

## Supersession

Any incompatible change requires a new ADR that explicitly supersedes this record.
