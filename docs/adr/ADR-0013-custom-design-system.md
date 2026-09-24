# ADR-0013: Custom design system

Status: **ACCEPTED**  
Date: **2026-09-23**

## Context

This decision is part of Architecture Baseline v1.0 for the local financial platform.

## Decision

Use a custom investing-oriented design system on Angular/CDK primitives rather than default Angular Material visual identity.

## Rationale

The dashboard is a core product experience and must be responsive/user-friendly.

## Consequences and constraints

Implementations must conform to this decision and must not hide an incompatible design behind an adapter or temporary shortcut. Tests and architecture checks should enforce the decision where practical.

## Revisit trigger

Accessibility and semantic financial states are first-class.

## Supersession

Any incompatible change requires a new ADR that explicitly supersedes this record.
