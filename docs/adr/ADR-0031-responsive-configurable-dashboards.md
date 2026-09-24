# ADR-0031: Responsive configurable dashboards

Status: **ACCEPTED**  
Date: **2026-09-23**

## Context

This decision is part of Architecture Baseline v1.0 for the local financial platform.

## Decision

Maintain separate responsive layout behavior for mobile/tablet/desktop and a widget registry/configuration model.

## Rationale

User explicitly requires high-quality investing dashboards across devices.

## Consequences and constraints

Implementations must conform to this decision and must not hide an incompatible design behind an adapter or temporary shortcut. Tests and architecture checks should enforce the decision where practical.

## Revisit trigger

Device layouts may differ rather than merely resize.

## Supersession

Any incompatible change requires a new ADR that explicitly supersedes this record.
